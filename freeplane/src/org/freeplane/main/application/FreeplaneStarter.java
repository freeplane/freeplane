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
import java.awt.Frame;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.FreeplaneVersion;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.ShowSelectionAsRectangleAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.common.attribute.ModelessAttributeController;
import org.freeplane.features.common.filter.FilterController;
import org.freeplane.features.common.filter.NextNodeAction;
import org.freeplane.features.common.icon.IconController;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.note.NoteController;
import org.freeplane.features.common.styles.LogicalStyleFilterController;
import org.freeplane.features.common.styles.MapViewLayout;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.common.text.TextController.Direction;
import org.freeplane.features.common.time.TimeController;
import org.freeplane.features.controller.help.HelpController;
import org.freeplane.features.controller.print.PrintController;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.main.browsemode.BModeControllerFactory;
import org.freeplane.main.filemode.FModeControllerFactory;
import org.freeplane.main.mindmapmode.MModeControllerFactory;
import org.freeplane.view.swing.addins.nodehistory.NodeHistory;
import org.freeplane.view.swing.map.MMapViewController;
import org.freeplane.view.swing.map.ViewLayoutTypeAction;

public class FreeplaneStarter {
	private static final String PROPERTIES_FOLDER = ".freeplane";

	public static String getResourceBaseDir() {
		return System.getProperty(FreeplaneStarter.ORG_FREEPLANE_GLOBALRESOURCEDIR,
		    FreeplaneStarter.DEFAULT_ORG_FREEPLANE_GLOBALRESOURCEDIR);
	}

	public static String getFreeplaneUserDirectory() {
		return System.getProperty("user.home") + File.separator + PROPERTIES_FOLDER;
	}

	public static void showSysInfo() {
		final StringBuilder info = new StringBuilder();
		info.append("freeplane_version = ");
		info.append(FreeplaneVersion.getVersion());
		info.append("; freeplane_xml_version = ");
		info.append(FreeplaneVersion.XML_VERSION);
		info.append("\njava_version = ");
		info.append(System.getProperty("java.version"));
		info.append("; os_name = ");
		info.append(System.getProperty("os.name"));
		info.append("; os_version = ");
		info.append(System.getProperty("os.version"));
		LogUtils.info(info.toString());
	}

	private ApplicationResourceController applicationResourceController;
// // 	private Controller controller;
	private FreeplaneSplashModern splash;
	private ApplicationViewController viewController;
	/** allows to disable loadLastMap(s) if there already is a second instance running. */
	private boolean dontLoadLastMaps;
	public static final String DEFAULT_ORG_FREEPLANE_GLOBALRESOURCEDIR = "resources";
	public static final String ORG_FREEPLANE_GLOBALRESOURCEDIR = "org.freeplane.globalresourcedir";

	public FreeplaneStarter() {
		super();
		applicationResourceController = new ApplicationResourceController();
		ResourceController.setResourceController(applicationResourceController);
	}

	public void setDontLoadLastMaps() {
		dontLoadLastMaps = true;
    }

	public Controller createController() {
		try {
			Controller controller = new Controller();
			Compat.macAppChanges();
			applicationResourceController.init();
			LogUtils.createLogger();
			final String lookandfeel = System.getProperty("lookandfeel", applicationResourceController
			    .getProperty("lookandfeel"));
			ViewController.setLookAndFeel(lookandfeel);
			final JFrame frame = new JFrame("Freeplane");
			frame.setName(UITools.MAIN_FREEPLANE_FRAME);
			splash = new FreeplaneSplashModern(frame);
			if (!System.getProperty("org.freeplane.nosplash", "false").equals("true")) {
				splash.setVisible(true);
			}
			final MMapViewController mapViewController = new MMapViewController();
			viewController = new ApplicationViewController(controller, mapViewController, frame);
			System.setSecurityManager(new FreeplaneSecurityManager());
			mapViewController.addMapViewChangeListener(applicationResourceController.getLastOpenedList());
			FilterController.install();
			PrintController.install();
			ModelessAttributeController.install();
			TextController.install();
			NoteController.install();
			TimeController.install();
			LinkController.install();
			IconController.install();
			HelpController.install();
			controller.addAction(new UpdateCheckAction());
			controller.addAction(new NextNodeAction(Direction.FORWARD));
			controller.addAction(new NextNodeAction(Direction.BACK));
			controller.addAction(new NextNodeAction(Direction.FORWARD_N_FOLD));
			controller.addAction(new NextNodeAction(Direction.BACK_N_FOLD));
			controller.addAction(new ShowSelectionAsRectangleAction());
			controller.addAction(new ViewLayoutTypeAction(MapViewLayout.OUTLINE));
			FilterController.getCurrentFilterController().getConditionFactory().addConditionController(7,
			    new LogicalStyleFilterController());

			NodeHistory.install(controller);
			MModeControllerFactory.createModeController();
			controller.getModeController(MModeController.MODENAME).getMapController().addMapChangeListener(
			    applicationResourceController.getLastOpenedList());
			BModeControllerFactory.createModeController("/xml/browsemodemenu.xml");
			FModeControllerFactory.createModeController();
			return controller;
		}
		catch (final Exception e) {
			LogUtils.severe(e);
			throw new RuntimeException(e);
		}
	}

	public void createFrame(final Controller controller,  final String[] args) {
		Compat.macMenuChanges();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				viewController.init(controller);
				splash.toBack();
				loadMaps(args);
				final Frame frame = viewController.getFrame();
				final int extendedState = frame.getExtendedState();
				frame.setVisible(true);
				if (extendedState != frame.getExtendedState()) {
					frame.setExtendedState(extendedState);
				}
				splash.dispose();
				splash = null;
				frame.toFront();
			}
		});
	}

	private void loadMaps( final String[] args) {
		final Controller controller = Controller.getCurrentController();
		final boolean alwaysLoadLastMaps = ResourceController.getResourceController().getBooleanProperty(
		    "always_load_last_maps");
		if (alwaysLoadLastMaps && !dontLoadLastMaps) {
			applicationResourceController.getLastOpenedList().openMapsOnStart();
		}
		boolean fileLoaded = false;
		for (int i = 0; i < args.length; i++) {
			String fileArgument = args[i];
			if (fileArgument.toLowerCase().endsWith(
			    org.freeplane.features.common.url.UrlManager.FREEPLANE_FILE_EXTENSION)) {
				if (!FileUtils.isAbsolutePath(fileArgument)) {
					fileArgument = System.getProperty("user.dir") + System.getProperty("file.separator") + fileArgument;
				}
				try {
					if (!fileLoaded) {
						controller.selectMode(MModeController.MODENAME);
					}
					final MModeController modeController = (MModeController) controller.getModeController();
					modeController.getMapController().newMap(Compat.fileToUrl(new File(fileArgument)));
					fileLoaded = true;
				}
				catch (final Exception ex) {
					System.err.println("File " + fileArgument + " not found error");
				}
			}
		}
		if (fileLoaded) {
			return;
		}
		if (!alwaysLoadLastMaps && !dontLoadLastMaps) {
			applicationResourceController.getLastOpenedList().openMapsOnStart();
		}
		
		if (null != controller.getMap()) {
			return;
		}
		controller.selectMode(MModeController.MODENAME);
		final MModeController modeController = (MModeController) controller.getModeController();
		modeController.getMapController().newMap(((NodeModel) null));
	}

	/**
	 */
	public void run(final String[] args) {
		try {
			if (null == System.getProperty("org.freeplane.core.dir.lib", null)) {
				System.setProperty("org.freeplane.core.dir.lib", "/lib/");
			}
			Controller controller = createController();
			createFrame(controller, args);
		}
		catch (final Exception e) {
			LogUtils.severe(e);
			JOptionPane.showMessageDialog(UITools.getFrame(), "freeplane.main.Freeplane can't be started",
			    "Startup problem", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	public void stop() {
		try {
			if (EventQueue.isDispatchThread()) {
				Controller.getCurrentController().shutdown();
				return;
			}
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					Controller.getCurrentController().shutdown();
				}
			});
		}
		catch (final InterruptedException e) {
			LogUtils.severe(e);
		}
		catch (final InvocationTargetException e) {
			LogUtils.severe(e);
		}
	}
}
