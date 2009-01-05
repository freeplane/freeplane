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
package org.freeplane.startup;

import java.awt.BorderLayout;

import javax.swing.JApplet;
import javax.swing.UIManager;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.AppletViewController;
import org.freeplane.core.resources.AppletResourceController;
import org.freeplane.features.browsemode.BModeController;
import org.freeplane.features.browsemode.BModeControllerFactory;

public class FreeplaneApplet extends JApplet {
	private AppletViewController appletViewController;
	private AppletResourceController resourceController;

	@Override
	public void init() {
		createRootPane();
		updateLookAndFeel();
		getContentPane().setLayout(new BorderLayout());
		resourceController = new AppletResourceController(this);
		final Controller controller = new Controller(resourceController);
		appletViewController = new AppletViewController(this);
		appletViewController.init();
		final BModeController browseController = BModeControllerFactory.createModeController();
		controller.selectMode(browseController);
	}

	@Override
	public void start() {
		appletViewController.start();
	}

	private void updateLookAndFeel() {
		String lookAndFeel = "";
		try {
			resourceController.setPropertyByParameter("lookandfeel");
			lookAndFeel = resourceController.getProperty("lookandfeel");
			if (lookAndFeel.equals("windows")) {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			}
			else if (lookAndFeel.equals("motif")) {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
			}
			else if (lookAndFeel.equals("mac")) {
				UIManager.setLookAndFeel("javax.swing.plaf.mac.MacLookAndFeel");
			}
			else if (lookAndFeel.equals("metal")) {
				UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			}
			else if (lookAndFeel.equals("gtk")) {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
			}
			else if (lookAndFeel.equals("nothing")) {
			}
			else if (lookAndFeel.indexOf('.') != -1) {
				UIManager.setLookAndFeel(lookAndFeel);
			}
			else {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
		}
		catch (final Exception ex) {
			System.err.println("Error while setting Look&Feel" + lookAndFeel);
		}
	}
}
