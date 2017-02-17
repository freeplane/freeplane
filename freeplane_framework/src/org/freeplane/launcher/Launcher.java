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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.knopflerfish.framework.Main;

public class Launcher {
	private File frameworkDir;
	private int argCount;

	public Launcher() {
		frameworkDir = getPathToJar();
		argCount = 0;
	}

	public static void main(String[] args) {
		new Launcher().launch(args);
	}


	private void launch(String[] args) {
		setDefines();
		setArgProperties(args);
		run();
	}

	private void setDefines() {
		setDefine("org.knopflerfish.framework.readonly", "true");
		setDefine("org.knopflerfish.gosg.jars", "reference:file:" + getAbsolutePath("core") + '/');
		if(isDefineNotSet("org.freeplane.globalresourcedir"))
			setDefine("org.freeplane.globalresourcedir", getAbsolutePath("resources"));
		
	}

	private boolean isDefineNotSet(String name) {
		return System.getProperty(name, null) == null;
	}

	private String setDefine(String name, String value) {
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

	private String getAbsolutePath(String relativePath) {
		return new File(frameworkDir, relativePath).getAbsolutePath();
	}

	private File getPathToJar() {
		URL frameworkUrl = Main.class.getProtectionDomain().getCodeSource().getLocation();
		try {
			return new File(frameworkUrl.toURI()).getAbsoluteFile().getParentFile();
		} catch (URISyntaxException e) {
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
