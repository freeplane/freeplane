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
package org.freeplane.main;

import java.awt.EventQueue;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.freeplane.controller.Controller;
import org.freeplane.controller.help.HelpController;
import org.freeplane.controller.print.PrintController;
import org.freeplane.controller.resources.ApplicationResourceController;
import org.freeplane.controller.views.ApplicationViewController;
import org.freeplane.controller.views.ViewController;
import org.freeplane.map.attribute.ModelessAttributeController;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.ModeController;
import org.freeplane.modes.browsemode.BModeControllerFactory;
import org.freeplane.modes.filemode.FModeControllerFactory;
import org.freeplane.modes.mindmapmode.MModeController;
import org.freeplane.modes.mindmapmode.MModeControllerFactory;
import org.freeplane.service.filter.FilterController;

public class FreemindStarter {
	public static final String LOAD_LAST_MAP = "load_last_map";

	static void checkJavaVersion() {
		System.out.println("Checking Java Version...");
		if (Controller.JAVA_VERSION.compareTo("1.5.0") < 0) {
			final String message = "Warning: FreeMind requires version Java 1.5.0 or higher (your version: "
			        + Controller.JAVA_VERSION
			        + ", installed in "
			        + System.getProperty("java.home") + ").";
			System.err.println(message);
			JOptionPane.showMessageDialog(null, message, "FreeMind",
			    JOptionPane.WARNING_MESSAGE);
			System.exit(1);
		}
	}

	static public void main(final String[] args) {
		FreemindStarter.checkJavaVersion();
		final FreemindStarter starter = new FreemindStarter();
		starter.run(args);
	}

	private Controller controller;

	public FreemindStarter() {
		super();
		final StringBuffer info = new StringBuffer();
		info.append("freemind_version = ");
		info.append(Controller.VERSION);
		info.append("; freemind_xml_version = ");
		info.append(Controller.XML_VERSION);
		info.append("\njava_version = ");
		info.append(System.getProperty("java.version"));
		info.append("; os_name = ");
		info.append(System.getProperty("os.name"));
		info.append("; os_version = ");
		info.append(System.getProperty("os.version"));
	}

	private ModeController createModeController(final String[] args) {
		Controller.getController();
		final ModeController ctrl = Controller
		    .getModeController();
		try {
			final Class macClass = Class
			    .forName("accessories.plugins.MacChanges");
			macClass.getConstructors()[0].newInstance(new Object[] { this });
		}
		catch (final Exception e1) {
		}
		return ctrl;
	}

	private void loadMaps(final String[] args,
	                      final ModeController pModeController) {
		boolean fileLoaded = false;
		for (int i = 0; i < args.length; i++) {
			String fileArgument = args[i];
			if (fileArgument
			    .toLowerCase()
			    .endsWith(
			        org.freeplane.io.url.mindmapmode.FileManager.FREEMIND_FILE_EXTENSION)) {
				if (!Tools.isAbsolutePath(fileArgument)) {
					fileArgument = System.getProperty("user.dir")
					        + System.getProperty("file.separator")
					        + fileArgument;
				}
				try {
					((MModeController) pModeController).getMapController()
					    .newMap(Tools.fileToUrl(new File(fileArgument)));
					fileLoaded = true;
				}
				catch (final Exception ex) {
					System.err.println("File " + fileArgument
					        + " not found error");
				}
			}
		}
		if (!fileLoaded) {
			final String restoreable = Controller.getResourceController()
			    .getProperty(Controller.ON_START_IF_NOT_SPECIFIED);
			if (Tools.isPreferenceTrue(Controller.getResourceController()
			    .getProperty(FreemindStarter.LOAD_LAST_MAP))
			        && restoreable != null && restoreable.length() > 0) {
				try {
					Controller.getController().getViewController()
					    .getLastOpenedList().open(restoreable);
					fileLoaded = true;
				}
				catch (final Exception e) {
					org.freeplane.main.Tools.logException(e);
					Controller.getController().getViewController().out(
					    "An error occured on opening the file: " + restoreable
					            + ".");
				}
			}
		}
		if (!fileLoaded) {
			/*
			 * nothing loaded so far. Perhaps, we should display a new map...
			 * According to Summary: On first start FreeMind should show new map
			 * to newbies https: &aid=1752516&group_id=7118
			 */
			pModeController.getMapController().newMap(((NodeModel) null));
		}
	}

	/**
	 */
	public void run(final String[] args) {
		try {
			final ApplicationResourceController resourceController = new ApplicationResourceController();
			controller = new Controller(resourceController);
			final IFreeMindSplash splash = new FreeMindSplashModern();
			splash.setVisible(true);
			final IFeedBack feedBack = splash.getFeedBack();
			feedBack.setMaximumValue(9);
			/* This is only for apple but does not harm for the others. */
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			feedBack.increase("Freeplane.progress.updateLookAndFeel");
			updateLookAndFeel();
			feedBack.increase("Freeplane.progress.createController");
			System.setSecurityManager(new FreeMindSecurityManager());
			final ApplicationViewController viewController = new ApplicationViewController();
			controller.setFilterController(new FilterController());
			controller.setPrintController(new PrintController());
			controller
			    .setAttributeController(new ModelessAttributeController());
			controller.setHelpController(new HelpController());
			MModeControllerFactory.createModeController();
			BModeControllerFactory.createModeController();
			FModeControllerFactory.createModeController();
			feedBack.increase("Freeplane.progress.settingPreferences");
			controller.getViewController().changeAntialias(
			    Controller.getResourceController().getProperty(
			        ViewController.RESOURCE_ANTIALIAS));
			feedBack.increase("Freeplane.progress.propagateLookAndFeel");
			SwingUtilities.updateComponentTreeUI(Controller.getController()
			    .getViewController().getJFrame());
			feedBack.increase("Freeplane.progress.buildScreen");
			viewController.init();
			try {
				if (!EventQueue.isDispatchThread()) {
					EventQueue.invokeAndWait(new Runnable() {
						public void run() {
						};
					});
				}
			}
			catch (final Exception e) {
				org.freeplane.main.Tools.logException(e);
			}
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					feedBack.increase("Freeplane.progress.createInitialMode");
					controller.selectMode(Controller.getResourceController()
					    .getProperty("initial_mode"));
					feedBack
					    .increase("Freeplane.progress.startCreateController");
					final ModeController ctrl = createModeController(args);
					feedBack.increase("Freeplane.progress.loadMaps");
					loadMaps(args, ctrl);
					feedBack.increase("Freeplane.progress.endStartup");
					if (splash != null) {
						splash.setVisible(false);
					}
					Controller.getController().getViewController().getJFrame()
					    .setVisible(true);
				}
			});
		}
		catch (final Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
			    "freemind.main.FreeMind can't be started", "Startup problem",
			    JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	/**
	 *
	 */
	private void updateLookAndFeel() {
		try {
			final String lookAndFeel = Controller.getResourceController()
			    .getProperty("lookandfeel");
			if (lookAndFeel.equals("windows")) {
				UIManager
				    .setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			}
			else if (lookAndFeel.equals("motif")) {
				UIManager
				    .setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
			}
			else if (lookAndFeel.equals("mac")) {
				UIManager.setLookAndFeel("javax.swing.plaf.mac.MacLookAndFeel");
			}
			else if (lookAndFeel.equals("metal")) {
				UIManager
				    .setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			}
			else if (lookAndFeel.equals("gtk")) {
				UIManager
				    .setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
			}
			else if (lookAndFeel.equals("nothing")) {
			}
			else if (lookAndFeel.indexOf('.') != -1) {
				UIManager.setLookAndFeel(lookAndFeel);
			}
			else {
				UIManager.setLookAndFeel(UIManager
				    .getSystemLookAndFeelClassName());
			}
		}
		catch (final Exception ex) {
			System.err.println("Unable to set Look & Feel.");
		}
	}
}
