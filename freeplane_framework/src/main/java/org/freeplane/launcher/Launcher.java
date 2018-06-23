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
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;

public class Launcher {
	private static final String DISABLE_SECURITY_MANAGER_PROPERTY = "org.freeplane.main.application.FreeplaneSecurityManager.disable";
	private static final String HEADLESS_PROPERTY = "org.freeplane.main.application.FreeplaneStarter.headless";
	private static final String BASEDIRECTORY_PROPERTY = "org.freeplane.basedirectory";
	private final File freeplaneInstallationDirectory;
	private int argCount;
	private boolean disableSecurityManager;
	private boolean freeplaneLaunched;
	
	private static AtomicBoolean launcherCreated = new AtomicBoolean(false);

	private Launcher() {
		this(getFreeplaneInstallationDirectory());
	}
	
	public static Launcher forInstallation(final File freeplaneInstallationDirectory) {
		System.setProperty(BASEDIRECTORY_PROPERTY, freeplaneInstallationDirectory.getPath());
		return new Launcher(freeplaneInstallationDirectory);
	}
	
	public HeadlessMapCreator launchHeadless() {
		System.setProperty(HEADLESS_PROPERTY, "true");
		return launchWithUI(new String[] {});
	}

	public Launcher disableSecurityManager() {
		disableSecurityManager = true;
		return this;
	}

	private Launcher(final File freeplaneInstallationDirectory) {
		if (! launcherCreated.compareAndSet(false, true)) 
			throw new IllegalStateException("Launcher instance already created");
		this.freeplaneInstallationDirectory = freeplaneInstallationDirectory;
		argCount = 0;
		disableSecurityManager = Boolean.getBoolean(DISABLE_SECURITY_MANAGER_PROPERTY);
	}
	
	static private File getFreeplaneInstallationDirectory() {
		final File frameworkDir;
		if (Utils.isDefineNotSet(BASEDIRECTORY_PROPERTY)) {
			frameworkDir = Utils.getPathToJar(Main.class);
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

	public static void main(String[] args) {
		fixX11AppName();
		workAroundForDataFlavorComparator_JDK8130242();
		new Launcher().launchWithoutUICheck(args);
	}


	private static void workAroundForDataFlavorComparator_JDK8130242() {
		final String javaVersion = System.getProperty("java.version");
		if(javaVersion.startsWith("1.7.") || javaVersion.startsWith("1.8."))
			System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
	}

	public Controller launchWithUI(String[] args) {
		System.setProperty(HEADLESS_PROPERTY, "false");
		return launchWithoutUICheck(args);
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
		return startFramework();
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

		final Framework framework = main.start(args);
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
