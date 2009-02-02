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

import java.awt.EventQueue;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.filter.FilterController;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ApplicationResourceController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.url.UrlManager;
import org.freeplane.core.util.Tools;
import org.freeplane.features.common.attribute.ModelessAttributeController;
import org.freeplane.features.controller.help.HelpController;
import org.freeplane.features.controller.print.PrintController;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.main.browsemode.BModeControllerFactory;
import org.freeplane.main.filemode.FModeControllerFactory;
import org.freeplane.main.mindmapmode.MModeControllerFactory;
import org.freeplane.view.swing.map.MMapViewController;

public class FreeplaneStarter {
	public static final String LOAD_LAST_MAP = "load_last_map";
	static private boolean loggerCreated = false;

	static public void main(final String[] args) {
		final FreeplaneStarter starter = new FreeplaneStarter();
		starter.run(args);
	}

	private Controller controller;
	private IFeedBack feedBack;
	private ApplicationResourceController resourceController;
	private IFreeplaneSplash splash;
	private ApplicationViewController viewController;

	public FreeplaneStarter() {
		super();
		checkJavaVersion();
		final StringBuffer info = new StringBuffer();
		info.append("freeplane_version = ");
		info.append(Controller.VERSION);
		info.append("; freeplane_xml_version = ");
		info.append(Controller.XML_VERSION);
		info.append("\njava_version = ");
		info.append(System.getProperty("java.version"));
		info.append("; os_name = ");
		info.append(System.getProperty("os.name"));
		info.append("; os_version = ");
		info.append(System.getProperty("os.version"));
	}

	void checkJavaVersion() {
		System.out.println("Checking Java Version...");
		if (Controller.JAVA_VERSION.compareTo("1.5.0") < 0) {
			final String message = "Warning: Freeplane requires version Java 1.5.0 or higher (your version: "
			        + Controller.JAVA_VERSION + ", installed in " + System.getProperty("java.home") + ").";
			System.err.println(message);
			JOptionPane.showMessageDialog(null, message, "Freeplane", JOptionPane.WARNING_MESSAGE);
			System.exit(1);
		}
	}

	public void createController() {
		resourceController = new ApplicationResourceController();
		Controller.setResourceController(resourceController);
		controller = new Controller();
		resourceController.init(controller);
		createLogger();
		splash = new FreeplaneSplashModern();
		splash.setVisible(true);
		feedBack = splash.getFeedBack();
		feedBack.setMaximumValue(9);
		/* This is only for apple but does not harm for the others. */
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		feedBack.increase("Freeplane.progress.updateLookAndFeel");
		updateLookAndFeel();
		feedBack.increase("Freeplane.progress.createController");
		//try {
		//	Thread.sleep(100000);
		//}
		//catch (InterruptedException e) {
		//	// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}	    
		System.setSecurityManager(new FreeplaneSecurityManager());
		final MMapViewController mapViewController = new MMapViewController();
		mapViewController.addMapChangeListener(resourceController.getLastOpenedList());
		viewController = new ApplicationViewController(controller, mapViewController);
		FilterController.install(controller);
		PrintController.install(controller);
		ModelessAttributeController.install(controller);
		HelpController.install(controller);
		MModeControllerFactory.createModeController(controller);
		BModeControllerFactory.createModeController(controller, "/xml/browsemodemenu.xml");
		FModeControllerFactory.createModeController(controller);
	}

	public void createFrame(final String[] args) {
		feedBack.increase("Freeplane.progress.settingPreferences");
		controller.getViewController().changeAntialias(
		    Controller.getResourceController().getProperty(ViewController.RESOURCE_ANTIALIAS));
		feedBack.increase("Freeplane.progress.propagateLookAndFeel");
		SwingUtilities.updateComponentTreeUI(controller.getViewController().getFrame());
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
			org.freeplane.core.util.Tools.logException(e);
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				feedBack.increase("Freeplane.progress.createInitialMode");
				controller.selectMode(Controller.getResourceController().getProperty("initial_mode"));
				feedBack.increase("Freeplane.progress.startCreateController");
				final ModeController ctrl = createModeController(args);
				feedBack.increase("Freeplane.progress.loadMaps");
				loadMaps(args, ctrl);
				feedBack.increase("Freeplane.progress.endStartup");
				if (splash != null) {
					splash.setVisible(false);
				}
				controller.getViewController().getFrame().setVisible(true);
			}
		});
	}

	private void createLogger() {
		if (loggerCreated) {
			return;
		}
		loggerCreated = true;
		final ResourceController resourceController = Controller.getResourceController();
		FileHandler mFileHandler = null;
		final Logger parentLogger = Logger.getAnonymousLogger().getParent();
		final Handler[] handlers = parentLogger.getHandlers();
		for (int i = 0; i < handlers.length; i++) {
			final Handler handler = handlers[i];
			if (handler instanceof ConsoleHandler) {
				parentLogger.removeHandler(handler);
			}
		}
		try {
			mFileHandler = new FileHandler(resourceController.getFreeplaneUserDirectory() + File.separator + "log",
			    1400000, 5, false);
			mFileHandler.setFormatter(new StdFormatter());
			mFileHandler.setLevel(Level.INFO);
			parentLogger.addHandler(mFileHandler);
			final ConsoleHandler stdConsoleHandler = new ConsoleHandler();
			stdConsoleHandler.setFormatter(new StdFormatter());
			stdConsoleHandler.setLevel(Level.WARNING);
			parentLogger.addHandler(stdConsoleHandler);
			LoggingOutputStream los;
			Logger logger = Logger.getLogger(StdFormatter.STDOUT.getName());
			los = new LoggingOutputStream(logger, StdFormatter.STDOUT);
			System.setOut(new PrintStream(los, true));
			logger = Logger.getLogger(StdFormatter.STDERR.getName());
			los = new LoggingOutputStream(logger, StdFormatter.STDERR);
			System.setErr(new PrintStream(los, true));
		}
		catch (final Exception e) {
			System.err.println("Error creating logging File Handler");
			e.printStackTrace();
		}
	}

	private ModeController createModeController(final String[] args) {
		final ModeController ctrl = controller.getModeController();
		try {
			final Class macClass = Class.forName("accessories.plugins.MacChanges");
			macClass.getConstructors()[0].newInstance(new Object[] { this });
		}
		catch (final Exception e1) {
		}
		return ctrl;
	}

	private void loadMaps(final String[] args, final ModeController pModeController) {
		boolean fileLoaded = false;
		for (int i = 0; i < args.length; i++) {
			String fileArgument = args[i];
			if (fileArgument.toLowerCase().endsWith(
			    org.freeplane.features.mindmapmode.file.MFileManager.FREEPLANE_FILE_EXTENSION)) {
				if (!UrlManager.isAbsolutePath(fileArgument)) {
					fileArgument = System.getProperty("user.dir") + System.getProperty("file.separator") + fileArgument;
				}
				try {
					((MModeController) pModeController).getMapController().newMap(
					    UrlManager.fileToUrl(new File(fileArgument)));
					fileLoaded = true;
				}
				catch (final Exception ex) {
					System.err.println("File " + fileArgument + " not found error");
				}
			}
		}
		if (!fileLoaded) {
			final String restoreable = Controller.getResourceController().getProperty(
			    Controller.ON_START_IF_NOT_SPECIFIED);
			if (Tools.isPreferenceTrue(Controller.getResourceController().getProperty(FreeplaneStarter.LOAD_LAST_MAP))
			        && restoreable != null && restoreable.length() > 0) {
				try {
					resourceController.getLastOpenedList().open(controller, restoreable);
					fileLoaded = true;
				}
				catch (final Exception e) {
					org.freeplane.core.util.Tools.logException(e);
					controller.getViewController().out("An error occured on opening the file: " + restoreable + ".");
				}
			}
		}
		if (!fileLoaded) {
			/*
			 * nothing loaded so far. Perhaps, we should display a new map...
			 * According to Summary: On first start Freeplane should show new map
			 * to newbies https: &aid=1752516&group_id=7118
			 */
			pModeController.getMapController().newMap(((NodeModel) null));
		}
	}

	/**
	 */
	public void run(final String[] args) {
		try {
			createController();
			createFrame(args);
		}
		catch (final Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "freeplane.main.Freeplane can't be started", "Startup problem",
			    JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	public void stop() {
		try {
			if (EventQueue.isDispatchThread()) {
				controller.shutdown();
				return;
			}
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					controller.shutdown();
				}
			});
		}
		catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (final InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 *
	 */
	private void updateLookAndFeel() {
		try {
			final String lookAndFeel = Controller.getResourceController().getProperty("lookandfeel");
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
			System.err.println("Unable to set Look & Feel.");
		}
	}
}
