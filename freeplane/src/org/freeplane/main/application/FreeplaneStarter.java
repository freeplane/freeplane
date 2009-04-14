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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.Compat;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.filter.FilterController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.ResourceControllerProperties;
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
	private FreeplaneSplashModern splash;
	private ApplicationViewController viewController;

	public FreeplaneStarter() {
		super();
		Compat.checkJavaVersion();
		Compat.showSysInfo();
	}

	public Controller createController() {
		try {
			applicationResourceController = new ApplicationResourceController();
			ResourceController.setResourceController(applicationResourceController);
			controller = new Controller();
			applicationResourceController.init(controller);
			LogTool.createLogger();
			final JFrame frame = new JFrame("Freeplane");
			Controller.setLookAndFeel(ResourceController.getResourceController().getProperty("lookandfeel"));
			Compat.useScreenMenuBar();
			splash = new FreeplaneSplashModern(null);
			feedBack = splash.getFeedBack();
			feedBack.setMaximumValue(3);
			final MMapViewController mapViewController = new MMapViewController();
			viewController = new ApplicationViewController(controller, mapViewController, frame);
			initFrame(frame);
			int extendedState = frame.getExtendedState();
			frame.setVisible(true);
			if(extendedState != frame.getExtendedState()){
				frame.setExtendedState(extendedState);
			}
			frame.setVisible(false);
			splash.setVisible(true);
			splash.toFront();
			feedBack.increase(FreeplaneSplashModern.FREEPLANE_PROGRESS_CREATE_CONTROLLER);
			System.setSecurityManager(new FreeplaneSecurityManager());
			mapViewController.addMapViewChangeListener(applicationResourceController.getLastOpenedList());
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
		catch (final Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public void initFrame(JFrame frame) {
		final ImageIcon mWindowIcon = new ImageIcon(ResourceController.getResourceController().getResource("/images/Freeplane_frame_icon.png"));
		frame.setIconImage(mWindowIcon.getImage());
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				controller.quit(new ActionEvent(this, 0, "quit"));
			}
			/*
			 * fc, 14.3.2008: Completely removed, as it damaged the focus if for
			 * example the note window was active.
			 */
		});
		if (StringUtils.equals(ResourceController.getResourceController().getProperty("toolbarVisible"), "false")) {
			controller.getViewController().setToolbarVisible(false);
		}
		if (StringUtils.equals(ResourceController.getResourceController().getProperty("leftToolbarVisible"), "false")) {
			controller.getViewController().setLeftToolbarVisible(false);
		}
		frame.setFocusTraversalKeysEnabled(false);
		int win_width = ResourceController.getResourceController().getIntProperty("appwindow_width", 0);
		int win_height = ResourceController.getResourceController().getIntProperty("appwindow_height", 0);
		int win_x = ResourceController.getResourceController().getIntProperty("appwindow_x", 0);
		int win_y = ResourceController.getResourceController().getIntProperty("appwindow_y", 0);
		win_width = (win_width > 0) ? win_width : 640;
		win_height = (win_height > 0) ? win_height : 440;
		final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
		final Insets screenInsets = defaultToolkit.getScreenInsets(frame.getGraphicsConfiguration());
		final Dimension screenSize = defaultToolkit.getScreenSize();
		final int screenWidth = screenSize.width - screenInsets.left - screenInsets.right;
		win_width = Math.min(win_width, screenWidth);
		final int screenHeight = screenSize.height - screenInsets.top - screenInsets.bottom;
		win_height = Math.min(win_height, screenHeight);
		win_x = Math.max(screenInsets.left, win_x);
		win_x = Math.min(screenWidth + screenInsets.left - win_width, win_x);
		win_y = Math.max(screenInsets.top, win_y);
		win_y = Math.min(screenWidth + screenInsets.top - win_height, win_y);
		frame.setBounds(win_x, win_y, win_width, win_height);
		int win_state = Integer
		    .parseInt(ResourceController.getResourceController().getProperty("appwindow_state", "0"));
		win_state = ((win_state & Frame.ICONIFIED) != 0) ? Frame.NORMAL : win_state;
		frame.setExtendedState(win_state);
	}
	

	public void createFrame(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				viewController.init();
				feedBack.increase("Freeplane.progress.startCreateController");
				final ModeController ctrl = createModeController(args);
				feedBack.increase("Freeplane.progress.loadMaps");
				loadMaps(args, ctrl);
				Frame frame = viewController.getFrame();
				int extendedState = frame.getExtendedState();
				frame.setVisible(true);
				if(extendedState != frame.getExtendedState()){
					frame.setExtendedState(extendedState);
				}
				splash.dispose();
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
			    org.freeplane.core.resources.ResourceControllerProperties.FREEPLANE_FILE_EXTENSION)) {
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
