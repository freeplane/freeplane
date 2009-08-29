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
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ToolTipManager;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.FreeplaneVersion;
import org.freeplane.core.filter.FilterController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.LimitedWidthTooltipUI;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.url.UrlManager;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.common.addins.misc.NextNodeAction;
import org.freeplane.features.common.addins.misc.NextNodeAction.Direction;
import org.freeplane.features.common.attribute.ModelessAttributeController;
import org.freeplane.features.common.icon.IconController;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.note.NoteController;
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
		LogTool.info(info.toString());
	}

	private ApplicationResourceController applicationResourceController;
	private Controller controller;
	private IFeedBack feedBack;
	private FreeplaneSplashModern splash;
	private ApplicationViewController viewController;

	public FreeplaneStarter() {
		super();
		Compat.checkJavaVersion();
		FreeplaneStarter.showSysInfo();
	}

	public Controller createController() {
		try {
			applicationResourceController = new ApplicationResourceController();
			ResourceController.setResourceController(applicationResourceController);
			controller = new Controller();
			applicationResourceController.init(controller);
			LogTool.createLogger();
			Controller.setLookAndFeel(applicationResourceController.getProperty("lookandfeel"));
			final JFrame frame = new JFrame("Freeplane");
			frame.setName(UITools.MAIN_FREEPLANE_FRAME);
			splash = new FreeplaneSplashModern(frame);
			if(! System.getProperty("org.freeplane.nosplash", "false").equals("true")){
				splash.setVisible(true);
			}
			Compat.useScreenMenuBar();
			feedBack = splash.getFeedBack();
			feedBack.setMaximumValue(2);
			final MMapViewController mapViewController = new MMapViewController();
			viewController = new ApplicationViewController(controller, mapViewController, frame);
			feedBack.increase(FreeplaneSplashModern.FREEPLANE_PROGRESS_CREATE_CONTROLLER);
			System.setSecurityManager(new FreeplaneSecurityManager());
			mapViewController.addMapViewChangeListener(applicationResourceController.getLastOpenedList());
			FilterController.install(controller);
			PrintController.install(controller);
			ModelessAttributeController.install(controller);
			TextController.install(controller);
			NoteController.install(controller);
			TimeController.install(controller);
			LinkController.install(controller);
			IconController.install(controller);
			HelpController.install(controller);
			controller.addAction(new UpdateCheckAction(controller));
			controller.addAction(new NextNodeAction(controller, Direction.FORWARD));
			controller.addAction(new NextNodeAction(controller, Direction.BACK));
			NodeHistory.install(controller);
			MModeControllerFactory.createModeController(controller);
			controller.getModeController(MModeController.MODENAME).getMapController().addMapChangeListener(
			    applicationResourceController.getLastOpenedList());
			BModeControllerFactory.createModeController(controller, "/xml/browsemodemenu.xml");
			FModeControllerFactory.createModeController(controller);
			return controller;
		}
		catch (final Exception e) {
			LogTool.severe(e);
			throw new RuntimeException(e);
		}
	}

	public void createFrame(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				viewController.init();
				try {
					final Class macClass = Class.forName("accessories.plugins.MacChanges");
					macClass.getConstructors()[0].newInstance(new Object[] { this });
				}
				catch (final Exception e) {
				}
				feedBack.increase("Freeplane.progress.loadMaps");
				loadMaps(args);
				final Frame frame = viewController.getFrame();
				final int extendedState = frame.getExtendedState();
				frame.setVisible(true);
				if (extendedState != frame.getExtendedState()) {
					frame.setExtendedState(extendedState);
				}
				splash.dispose();
				frame.toFront();
			}
		});
	}

	private void loadMaps(final String[] args) {
		boolean fileLoaded = false;
		for (int i = 0; i < args.length; i++) {
			String fileArgument = args[i];
			if (fileArgument.toLowerCase().endsWith(org.freeplane.core.url.UrlManager.FREEPLANE_FILE_EXTENSION)) {
				if (!UrlManager.isAbsolutePath(fileArgument)) {
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
		applicationResourceController.getLastOpenedList().openMapsOnStart();
		/*
		 * nothing loaded so far. Perhaps, we should display a new map...
		 * According to Summary: On first start Freeplane should show new map
		 * to newbies https: &aid=1752516&group_id=7118
		 */
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
			createController();
			createFrame(args);
		}
		catch (final Exception e) {
			LogTool.severe(e);
			JOptionPane.showMessageDialog(UITools.getFrame(), "freeplane.main.Freeplane can't be started",
			    "Startup problem", JOptionPane.ERROR_MESSAGE);
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
			LogTool.severe(e);
		}
		catch (final InvocationTargetException e) {
			LogTool.severe(e);
		}
	}
}
