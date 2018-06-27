/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2012 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.launcher;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.freeplane.api.Controller;
import org.freeplane.api.HeadlessMapCreator;
import org.knopflerfish.framework.Main;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
/**
 * This class can be used to run freeplane instance from an application and to obtain its {@link Controller} object.
 * 
 * To run a headless Freeplane instance use {@code Launcher.create().launchHeadless()},
 * to run a freeplane with complete user UI use {@code Launcher.create().launch()}
 * 
* <pre>
* 
* Code Example: 
* {@code
	public static void createNewMindMap(File freeplaneInstallationDirectory, final File newMapFile) {
		final Launcher launcher = Launcher.createForInstallation(freeplaneInstallationDirectory).disableSecurityManager();
		HeadlessMapCreator mapCreator = launcher.launchHeadless();
		final Map map = mapCreator.newHiddenMapFromTemplate(TestApp.class.getResource("/templateFile.mm"));
		map.getRoot().createChild().setText("hello world");
		map.saveAs(newMapFile);
		System.out.println("Saved file " + newMapFile.getAbsolutePath());
		launcher.shutdown();
	}
* }
* </pre>
 * 
 */
public class Launcher {
	private static final String DISABLE_SECURITY_MANAGER_PROPERTY = "org.freeplane.main.application.FreeplaneSecurityManager.disable";
	private static final String HEADLESS_PROPERTY = "org.freeplane.main.application.FreeplaneStarter.headless";
	private static final String BASEDIRECTORY_PROPERTY = "org.freeplane.basedirectory";
	private final File freeplaneInstallationDirectory;
	private int argCount;
	private boolean disableSecurityManager;
	private boolean freeplaneLaunched;
	private static AtomicBoolean launcherCreated = new AtomicBoolean(false);
	private Framework framework;
	
	public static void main(String[] args) {
		fixX11AppName();
		workAroundForDataFlavorComparator_JDK8130242();
		new Launcher().launchWithoutUICheck(args);
	}
	
	/**
	 * Creates Launcher for starting embedded Freeplane instance.
	 * 
	 * @param freeplaneInstallationDirectory Path to freeplane installation directory 
	 * 
	 *  Only one Launcher per JVM can be created.
	 *  
	 * @throws IllegalStateException is launcher already was created.
	 * 
	 */
	public static Launcher createForInstallation(final File freeplaneInstallationDirectory) {
		System.setProperty(BASEDIRECTORY_PROPERTY, freeplaneInstallationDirectory.getPath());
		return new Launcher(freeplaneInstallationDirectory);
	}


	/**
	 * Creates Launcher for starting embedded Freeplane instance.
	 * 
	 * Freeplane installation directory is defined by location of jar file containing this class. 
	 * 
	 * Only one Launcher per JVM can be created.
	 * 
	 * @throws IllegalStateException is launcher already was created.
	 */
	public static Launcher create() {
		return new Launcher();
	}

	private Launcher() {
		this(getFreeplaneInstallationDirectory());
	}
	
	
	/**
	 * Launchs Freeplane without UI and returns HeadlessMapCreator instance.
	 * 
	 * @throws IllegalStateException is Freeplane was already launched.
	 * 
	 */
	public HeadlessMapCreator launchHeadless() {
		System.setProperty(HEADLESS_PROPERTY, "true");
		return launchWithoutUICheck(new String[] {});
	}
	
	/**
	 * Launchs Freeplane with UI and returns Controller instance.
	 * 
	 * All API methods should be called from the swing event thread to avoid race conditions.
	 * 
	 * @throws IllegalStateException is Freeplane was already launched.
	 * 
	 */
	public Controller launchWithUI(String[] args) {
		System.setProperty(HEADLESS_PROPERTY, "false");
		return launchWithoutUICheck(args);
	}


    /**
     * This method can be used to shutdown embedded Freeplane instance.
     */
	public void shutdown(){
		if(framework != null)
			try {
				framework.stop();
			}
		catch (BundleException e) {
			throw new RuntimeException(e);
		}
	}

	
    /**
     * Disables security manager for launched Freeplane instance.
     */
	public Launcher disableSecurityManager() {
		disableSecurityManager = true;
		return this;
	}

	private Launcher(final File freeplaneInstallationDirectory) {
		ensureSingleInstance();
		this.freeplaneInstallationDirectory = freeplaneInstallationDirectory;
		argCount = 0;
		disableSecurityManager = Boolean.getBoolean(DISABLE_SECURITY_MANAGER_PROPERTY);
	}

	static private void ensureSingleInstance() {
		if (! launcherCreated.compareAndSet(false, true)) 
			throw new IllegalStateException("Launcher instance already created");
	}
	
	static private File getFreeplaneInstallationDirectory() {
		final File frameworkDir;
		if (Utils.isDefineNotSet(BASEDIRECTORY_PROPERTY)) {
			frameworkDir = Utils.getPathToJar(Launcher.class);
		}
		else {
			try {
				frameworkDir = new File(System.getProperty(BASEDIRECTORY_PROPERTY)).getCanonicalFile();
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return frameworkDir;
	}

	private static void fixX11AppName() {
		try {
			Toolkit xToolkit = Toolkit.getDefaultToolkit();
			if (xToolkit.getClass().getName().equals("sun.awt.X11.XToolkit"))
			{
				java.lang.reflect.Field awtAppClassNameField = xToolkit.getClass().getDeclaredField("awtAppClassName");
				awtAppClassNameField.setAccessible(true);
				awtAppClassNameField.set(xToolkit, "Freeplane");
			}
		} catch (NoSuchFieldException | SecurityException
				| IllegalArgumentException | IllegalAccessException e) {
			System.err.format("Couldn't set awtAppClassName: %s%n", e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	private static void workAroundForDataFlavorComparator_JDK8130242() {
		final String javaVersion = System.getProperty("java.version");
		if(javaVersion.startsWith("1.7.") || javaVersion.startsWith("1.8."))
			System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
	}

	private Controller launchWithoutUICheck(String[] args) {
		if(freeplaneLaunched)
			throw new IllegalStateException("Freeplane already launched");
		freeplaneLaunched = true;
		setDefines();
		if (! disableSecurityManager)
			System.setSecurityManager(new SecurityManager(){

				@Override
				public void checkConnect(String pHost, int pPort, Object pContext) {
					if(pContext != null)
						super.checkConnect(pHost, pPort, pContext);
					else
						super.checkConnect(pHost, pPort);
				}

			});
		setArgProperties(args);
		final Controller controller = startFramework();
		return controller;
	}

	private void setDefines() {
		Utils.setDefine("org.knopflerfish.framework.readonly", "true");
		Utils.setDefine("org.knopflerfish.gosg.jars", "reference:file:" + getAbsolutePath("core") + '/');
		Utils.setDefine("org.freeplane.user.dir", System.getProperty("user.dir"));
		Utils.setDefine(BASEDIRECTORY_PROPERTY, getAbsolutePath());
		System.setProperty("user.dir", getAbsolutePath());
		Utils.setDefineIfNeeded("org.freeplane.globalresourcedir", getAbsolutePath("resources"));
		Utils.setDefineIfNeeded("java.security.policy", getAbsolutePath("freeplane.policy"));
		Utils.setDefine("org.osgi.framework.storage", getAbsolutePath("fwdir"));
	}

	private Controller startFramework() {
		String[] args = new String[]{
				"-xargs",
				getAbsolutePath("props.xargs"),
				"-xargs",
				getAbsolutePath("init.xargs"),
				"-bg"
		};
		Main main = new Main();

		System.out.println(main.bootText);

		framework = main.start(args);
		final BundleContext bundleContext = framework.getBundleContext();
		final ServiceReference<Controller> controller = bundleContext.getServiceReference(Controller.class);
		final Controller service = bundleContext.getService(controller);
		return service;
	}

	private String getAbsolutePath() {
		return freeplaneInstallationDirectory.getAbsolutePath();
	}

	private String getAbsolutePath(String relativePath) {
		return new File(freeplaneInstallationDirectory, relativePath).getAbsolutePath();
	}

	private void setArgProperties(String[] args) {
		for(String arg:args){
			setArgumentProperty(arg);
		}
	}

	private void setArgumentProperty(String arg) {
		String propertyName = "org.freeplane.param" + ++argCount;
		System.setProperty(propertyName, arg);
	}
	
	

}
