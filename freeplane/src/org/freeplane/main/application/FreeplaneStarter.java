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
import java.lang.reflect.InvocationTargetException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.freeplane.core.Compat;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.enums.ResourceControllerProperties;
import org.freeplane.core.filter.FilterController;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.url.UrlManager;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.common.attribute.ModelessAttributeController;
import org.freeplane.features.common.icon.IconController;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.common.time.TimeController;
import org.freeplane.features.controller.help.HelpController;
import org.freeplane.features.controller.print.PrintController;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.main.browsemode.BModeControllerFactory;
import org.freeplane.main.filemode.FModeControllerFactory;
import org.freeplane.main.mindmapmode.MModeControllerFactory;
import org.freeplane.view.swing.addins.nodehistory.NodeHistory;
import org.freeplane.view.swing.map.MMapViewController;

public class FreeplaneStarter {
	static public void main(final String[] args) {
		final FreeplaneStarter starter = new FreeplaneStarter();
		starter.run(args);
	}

	private ApplicationResourceController applicationResourceController;
	private Controller controller;
	private IFeedBack feedBack;
	private IFreeplaneSplash splash;
	private ApplicationViewController viewController;

	public FreeplaneStarter() {
		super();
		Compat.checkJavaVersion();
		Compat.showSysInfo();
	}

	public Controller createController() {
		applicationResourceController = new ApplicationResourceController();
		ResourceController.setResourceController(applicationResourceController);
		controller = new Controller();
		applicationResourceController.init(controller);
		LogTool.createLogger();
		splash = new FreeplaneSplashModern();
		splash.setVisible(true);
		feedBack = splash.getFeedBack();
		feedBack.setMaximumValue(9);
		Compat.useScreenMenuBar();
		feedBack.increase(FreeplaneSplashModern.FREEPLANE_PROGRESS_UPDATE_LOOK_AND_FEEL);
		Controller.setLookAndFeel(ResourceController.getResourceController().getProperty("lookandfeel"));
		feedBack.increase(FreeplaneSplashModern.FREEPLANE_PROGRESS_CREATE_CONTROLLER);
		System.setSecurityManager(new FreeplaneSecurityManager());
		final MMapViewController mapViewController = new MMapViewController();
		mapViewController.addMapViewChangeListener(applicationResourceController.getLastOpenedList());
		viewController = new ApplicationViewController(controller, mapViewController);
		FilterController.install(controller);
		PrintController.install(controller);
		ModelessAttributeController.install(controller);
		TextController.install(controller);
		TimeController.install(controller);
		LinkController.install(controller);
		IconController.install(controller);
		HelpController.install(controller);
		NodeHistory.install(controller);
		MModeControllerFactory.createModeController(controller);
		BModeControllerFactory.createModeController(controller, "/xml/browsemodemenu.xml");
		FModeControllerFactory.createModeController(controller);
		return controller;
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
			LogTool.logException(e);
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
			if (Boolean.parseBoolean(ResourceController.getResourceController().getProperty(
			    ResourceControllerProperties.LOAD_LAST_MAP))
			        && restoreable != null && restoreable.length() > 0) {
				try {
					applicationResourceController.getLastOpenedList().open(controller, restoreable);
					fileLoaded = true;
				}
				catch (final Exception e) {
					LogTool.logException(e);
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
			e.printStackTrace();
		}
		catch (final InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
