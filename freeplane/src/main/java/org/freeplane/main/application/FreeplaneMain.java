/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.main.application;

import javax.swing.JOptionPane;

public class FreeplaneMain {
	public static void checkJavaVersion() {
		final String JAVA_VERSION = System.getProperty("java.version");
		final String VERSION_1_5_0 = "1.5.0";
		if (JAVA_VERSION.compareTo(VERSION_1_5_0) < 0) {
			final String message = "Warning: Freeplane requires version Java 1.5.0 or higher. The running version: "
			        + JAVA_VERSION + " is installed in " + System.getProperty("java.home") + ".";
			System.err.println(message);
			JOptionPane.showMessageDialog(null, message, "error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		final String osProperty = System.getProperty("os.name");
		if (osProperty.startsWith("Mac OS")) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
		}
	}

	static public void main(final String[] args) {
		FreeplaneMain.checkJavaVersion();
        final String oldHandler = System.getProperty("java.protocol.handler.pkgs");
        String newHandler = "org.freeplane.main.application.protocols";
        if(oldHandler != null)
            newHandler = oldHandler + '|' + newHandler;
        System.setProperty("java.protocol.handler.pkgs", newHandler);
		final FreeplaneStarter starter = new FreeplaneGUIStarter(args);
		starter.run(args);
	}
}
