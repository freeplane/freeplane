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

import org.freeplane.core.Compat;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.enums.ResourceControllerProperties;
import org.freeplane.core.filter.FilterController;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
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
	static private boolean loggerCreated = false;

	static public void main(final String[] args) {
		final FreeplaneStarter starter = new FreeplaneStarter();
		starter.run(args);
	}

	private Controller controller;
	private IFeedBack feedBack;
	private ApplicationResourceController applicationResourceController;
	private IFreeplaneSplash splash;
	private ApplicationViewController viewController;

	public FreeplaneStarter() {
		super();
		Compat.checkJavaVersion();
		Compat.showSysInfo();
	}

	public void createController() {
		
		applicationResourceController = new ApplicationResourceController();
		ResourceController.setResourceController(applicationResourceController);
		controller = new Controller();
		applicationResourceController.init(controller);
		createLogger();
		splash = new FreeplaneSplashModern();
		splash.setVisible(true);
		feedBack = splash.getFeedBack();
		feedBack.setMaximumValue(9);
		Compat.useScreenMenuBar();
		feedBack.increase(FreeplaneSplashModern.FREEPLANE_PROGRESS_UPDATE_LOOK_AND_FEEL);
		updateLookAndFeel();
		feedBack.increase(FreeplaneSplashModern.FREEPLANE_PROGRESS_CREATE_CONTROLLER);
		//try {
		//	Thread.sleep(100000);
		//}
		//catch (InterruptedException e) {
		//	// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}	    
		System.setSecurityManager(new FreeplaneSecurityManager());
		final MMapViewController mapViewController = new MMapViewController();
		mapViewController.addMapChangeListener(applicationResourceController.getLastOpenedList());
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
		    ResourceController.getResourceController().getProperty(ViewController.RESOURCE_ANTIALIAS));
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
			Tools.logException(e);
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				feedBack.increase("Freeplane.progress.createInitialMode");
				controller.selectMode(ResourceController.getResourceController().getProperty("initial_mode"));
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
		final ResourceController resourceController = ResourceController.getResourceController();
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
			    org.freeplane.core.enums.ResourceControllerProperties.FREEPLANE_FILE_EXTENSION)) {
				if (!UrlManager.isAbsolutePath(fileArgument)) {
					fileArgument = System.getProperty("user.dir") + System.getProperty("file.separator") + fileArgument;
				}
				try {
					((MModeController) pModeController).getMapController().newMap(
					    Compat.fileToUrl(new File(fileArgument)));
					fileLoaded = true;
				}
				catch (final Exception ex) {
					System.err.println("File " + fileArgument + " not found error");
				}
			}
		}
		if (!fileLoaded) {
			final String restoreable = ResourceController.getResourceController().getProperty(
			    ResourceControllerProperties.ON_START_IF_NOT_SPECIFIED);
			if (Boolean.parseBoolean(ResourceController.getResourceController().getProperty(ResourceControllerProperties.LOAD_LAST_MAP))
			        && restoreable != null && restoreable.length() > 0) {
				try {
					applicationResourceController.getLastOpenedList().open(controller, restoreable);
					fileLoaded = true;
				}
				catch (final Exception e) {
					Tools.logException(e);
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
			final String lookAndFeel = ResourceController.getResourceController().getProperty("lookandfeel");
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
