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
import java.net.URISyntaxException;
import java.net.URL;

import org.knopflerfish.framework.Main;

public class Launcher {
	private File frameworkDir;
	private int argCount;

	public Launcher() {
		if (isDefineNotSet("org.freeplane.basedirectory")) {
			frameworkDir = getPathToJar();
		}
		else {
			try {
				frameworkDir = new File(System.getProperty("org.freeplane.basedirectory")).getCanonicalFile();
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		argCount = 0;
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
		new Launcher().launch(args);
	}


	private static void workAroundForDataFlavorComparator_JDK8130242() {
		final String javaVersion = System.getProperty("java.version");
		if(javaVersion.startsWith("1.7.") || javaVersion.startsWith("1.8."))
			System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
	}

	private void launch(String[] args) {
		setDefines();
		setArgProperties(args);
		run();
	}

	private void setDefines() {
		setDefine("org.knopflerfish.framework.readonly", "true");
		setDefine("org.knopflerfish.gosg.jars", "reference:file:" + getAbsolutePath("core") + '/');
		setDefine("org.freeplane.basedirectory", getAbsolutePath());
		setDefineIfNeeded("org.freeplane.globalresourcedir", getAbsolutePath("resources"));
		setDefineIfNeeded("java.security.policy", getAbsolutePath("freeplane.policy"));
		setDefine("org.osgi.framework.storage", getAbsolutePath("fwdir"));
		System.setSecurityManager(new SecurityManager());
	}

	private void setDefineIfNeeded(String name, String value) {
		if (isDefineNotSet(name)) {
			setDefine(name, value);
		}
	}

	private boolean isDefineNotSet(String name) {
		return System.getProperty(name, null) == null;
	}

	private String setDefine(String name, String value) {
		System.out.println(name + "=" + value);
		return System.setProperty(name, value);
	}

	private void run() {
		String[] args = new String[]{
				"-xargs",
				getAbsolutePath("props.xargs"),
				"-xargs",
				getAbsolutePath("init.xargs")
		};
		Main.main(args);
	}

	private String getAbsolutePath() {
		return frameworkDir.getAbsolutePath();
	}

	private String getAbsolutePath(String relativePath) {
		return new File(frameworkDir, relativePath).getAbsolutePath();
	}

	private File getPathToJar() {
		URL frameworkUrl = Main.class.getProtectionDomain().getCodeSource().getLocation();
		try {
			return new File(frameworkUrl.toURI()).getCanonicalFile().getParentFile();
		}
		catch (URISyntaxException | IOException e) {
			throw new RuntimeException(e);
		}
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
