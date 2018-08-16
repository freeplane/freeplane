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
package org.freeplane.main.applet;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.html.parser.ParserDelegator;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.ShowSelectionAsRectangleAction;
import org.freeplane.features.attribute.ModelessAttributeController;
import org.freeplane.features.explorer.MapExplorerConditionController;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.filter.NextNodeAction;
import org.freeplane.features.filter.NextPresentationItemAction;
import org.freeplane.features.format.FormatController;
import org.freeplane.features.help.HelpController;
import org.freeplane.features.highlight.HighlightController;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapController.Direction;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.print.PrintController;
import org.freeplane.features.styles.LogicalStyleFilterController;
import org.freeplane.features.styles.MapViewLayout;
import org.freeplane.features.text.TextController;
import org.freeplane.features.time.TimeController;
import org.freeplane.features.ui.FrameController;
import org.freeplane.view.swing.features.nodehistory.NodeHistory;
import org.freeplane.view.swing.map.MapViewController;
import org.freeplane.view.swing.map.ViewLayoutTypeAction;

public class FreeplaneApplet extends JApplet {

	@SuppressWarnings("serial")
	private static class GlassPane extends JComponent{
		private final Controller controller;


		public GlassPane(Controller controller) {
			this.controller = controller;
			addMouseListener(new MouseAdapter(){});
		}


		@Override
        protected void processMouseEvent(MouseEvent e) {
			if (e.getID() == MouseEvent.MOUSE_EXITED){
				return;
			}
			Controller currentController = Controller.getCurrentController();
			if( controller != currentController ){
				if(! appletLock.tryLock()){
					return;
				}
				Controller.setCurrentController(controller);
				appletLock.unlock();
				JOptionPane.getFrameForComponent(this).getMostRecentFocusOwner().requestFocus();
				if(currentController != null){
					((FreeplaneApplet)(currentController.getViewController().getMenuComponent())).getGlassPane().setVisible(true);
				}
			}
			setVisible(false);
		}
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private AppletViewController appletViewController;
 	private Controller controller;
	private static boolean instanceInitialized = false;

 	final static Lock appletLock = new ReentrantLock();
	private Boolean isLaunchedByJavaWebStart;

	public FreeplaneApplet() throws HeadlessException {
	    super();
    }

	@Override
	public void destroy() {
	}

	@SuppressWarnings("serial")
    @Override
	public void init() {
		configureFrame();
		new Thread(new Runnable() {

			@Override
			public void run() {
				try{
					appletLock.lock();
					AppletResourceController appletResourceController = new AppletResourceController(FreeplaneApplet.this);
					new ParserDelegator(){
						{
							setDefaultDTD();
						}
					};
					controller = new Controller(appletResourceController);
					updateLookAndFeel(appletResourceController);
					Controller.setCurrentController(controller);
					final Container contentPane = getContentPane();
					contentPane.setLayout(new BorderLayout());
					MapViewController mapViewController = new MapViewController(controller);
					appletViewController = new AppletViewController(FreeplaneApplet.this, controller, mapViewController);
					controller.addAction(new ViewLayoutTypeAction(MapViewLayout.OUTLINE));
					controller.addExtension(HighlightController.class, new HighlightController());
					FilterController.install();
					PrintController.install();
					HelpController.install();
					NodeHistory.install(controller);
					FormatController.install(new FormatController());
					ModelessAttributeController.install();
					TextController.install();
					MapController.install();
					MapExplorerConditionController.installFilterConditions();
					TimeController.install();
					LinkController.install();
					IconController.installConditionControllers();
					FilterController.getCurrentFilterController().getConditionFactory().addConditionController(70,
					    new LogicalStyleFilterController());
					final BModeController browseController = BModeControllerFactory.createModeController();
					final Set<String> emptySet = Collections.emptySet();
					FilterController.getController(controller).loadDefaultConditions();
					controller.addAction(new ShowSelectionAsRectangleAction());
					controller.addAction(new NextNodeAction(Direction.FORWARD));
					controller.addAction(new NextNodeAction(Direction.BACK));
					controller.addAction(new NextNodeAction(Direction.FORWARD_N_FOLD));
					controller.addAction(new NextNodeAction(Direction.BACK_N_FOLD));
					controller.addAction(NextPresentationItemAction.createFoldingAction());
					controller.addAction(NextPresentationItemAction.createNotFoldingAction());
					browseController.updateMenus("/xml/appletmenu.xml", emptySet);
					appletResourceController.getAcceleratorManager().loadAcceleratorPresets();

					controller.selectMode(browseController);
					setPropertyByParameter(appletResourceController, "browsemode_initial_map");
					isLaunchedByJavaWebStart = isParameterTrue("launched_by_java_web_start");
					if(isLaunchedByJavaWebStart) {
						if(instanceInitialized)
							throw new RuntimeException("singleAppletInstance allowed");
						else
							instanceInitialized = true;
					} else
						addGlassPane();
					controller.getViewController().setMenubarVisible(false);
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							appletViewController.init(controller);
						}
					});
				}
				catch(RuntimeException e){
					e.printStackTrace();
					throw e;
				}
				finally{
					appletLock.unlock();
				}
			}
		}, "Freeplane applet initialization").start();
	}

	private Boolean isParameterTrue(String name) {
		return Boolean.valueOf(getParameter(name));
	}

	private void configureFrame() {
		Window window = SwingUtilities.windowForComponent(this);
		if (window instanceof Frame){
			Frame frame = (Frame)window;
			ImageIcon mWindowIcon;
			mWindowIcon = new ImageIcon(getClass().getResource(
					"/images/Freeplane_frame_icon_64x64.png"));
			frame.setIconImage(mWindowIcon.getImage());
			if (!frame.isResizable()){
				frame.setResizable(true);
			}
		}
	}

	private void addGlassPane() {
		final GlassPane glassPane = new GlassPane(controller);
		setGlassPane(glassPane);
		glassPane.setVisible(true);
	}

	@Override
	public void start() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				appletViewController.start();
			}
		});
	}

	@Override
	public void stop() {
		super.stop();
	}

	private void updateLookAndFeel(ResourceController appletResourceController) {
		String lookAndFeel = "";
		setPropertyByParameter(appletResourceController, "lookandfeel");
		lookAndFeel = appletResourceController.getProperty("lookandfeel");
		FrameController.setLookAndFeel(lookAndFeel, true);
	}

	@Override
    public Component findComponentAt(int x, int y) {
	    final Component c = super.findComponentAt(x, y);
	    if(c == null){
	    	return null;
	    }
		final AWTEvent currentEvent = EventQueue.getCurrentEvent();
		if(controller != Controller.getCurrentController()
				&& currentEvent instanceof MouseEvent
				&& currentEvent.getID() == MouseEvent.MOUSE_MOVED){
			if(appletLock.tryLock()){
				Controller.setCurrentController(controller);
				appletLock.unlock();
			}
		}
		return c;
	}

	public void setWaitingCursor(final boolean waiting) {
		Component glassPane = getRootPane().getGlassPane();
		if (waiting) {
			glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			glassPane.setVisible(true);
		}
		else {
			glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			glassPane.setVisible(false);
		}
	}

	void setPropertyByParameter(ResourceController resourceController, final String key) {
		final String val = getParameter(key);
		if (val != null && val != "") {
			resourceController.setProperty(key, val);
		}
	}

	public void showDocument(URL doc) {
		if(isLaunchedByJavaWebStart && Desktop.isDesktopSupported())
			try {
				Desktop.getDesktop().browse(doc.toURI());
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		getAppletContext().showDocument(doc, "_blank");
	}

}
