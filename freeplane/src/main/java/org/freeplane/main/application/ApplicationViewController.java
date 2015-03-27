/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.FrameController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.view.swing.map.MapView;

class ApplicationViewController extends FrameController {
	private static final String SPLIT_PANE_LAST_LEFT_POSITION = "split_pane_last_left_position";
	private static final String SPLIT_PANE_LAST_POSITION = "split_pane_last_position";
	private static final String SPLIT_PANE_LAST_RIGHT_POSITION = "split_pane_last_right_position";
	private static final String SPLIT_PANE_LAST_TOP_POSITION = "split_pane_last_top_position";
	private static final String SPLIT_PANE_LEFT_POSITION = "split_pane_left_position";
	private static final String SPLIT_PANE_POSITION = "split_pane_position";
	private static final String SPLIT_PANE_RIGHT_POSITION = "split_pane_right_position";
	private static final String SPLIT_PANE_TOP_POSITION = "split_pane_top_position";
// // 	final private Controller controller;
	final private JFrame frame;
	/** Contains the value where the Note Window should be displayed (right, left, top, bottom) */
	private String mLocationPreferenceValue;
	/** Contains the Note Window Component */
	private JComponent mMindMapComponent;
	final private JSplitPane mSplitPane;
	final private NavigationNextMapAction navigationNextMap;
	final private NavigationPreviousMapAction navigationPreviousMap;
	final private ResourceController resourceController;
	private JComponent mapPane;
	private MapViewDockingWindows mapViewWindows;
	@SuppressWarnings("serial")
    public ApplicationViewController( Controller controller, final IMapViewManager mapViewController,
	                                 final JFrame frame) {
		super(controller, mapViewController, "");
//		this.controller = controller;
		navigationPreviousMap = new NavigationPreviousMapAction();
		controller.addAction(navigationPreviousMap);
		navigationNextMap = new NavigationNextMapAction();
		controller.addAction(navigationNextMap);
		resourceController = ResourceController.getResourceController();
		this.frame = frame;
		getContentPane().setLayout(new BorderLayout());
		// --- Set Note Window Location ---
		mLocationPreferenceValue = resourceController.getProperty("note_location", "bottom");
		// disable all hotkeys for JSplitPane
		mSplitPane = new JSplitPane(){
			@Override
			protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed){
				return false;
			}
		};
		setSplitPaneLayoutManager();
		final Component contentPane;
		mapViewWindows = new MapViewDockingWindows();
		contentPane = mapViewWindows.getMapPane();
		getContentPane().add(contentPane, BorderLayout.CENTER);
		mapPane = mapViewWindows.getMapPane();
		getContentPane().add(mSplitPane, BorderLayout.CENTER);
		mSplitPane.setLeftComponent(mapPane);
		mSplitPane.setRightComponent(null);
		initFrame(frame);
	}

	/**
	 * Called from the Controller, when the Location of the Note Window is changed on the Menu->View->Note Window Location
	 */
	@Override
	public void changeNoteWindowLocation() {
		mLocationPreferenceValue = resourceController.getProperty("note_location");
		if(mMindMapComponent != null){
			insertComponentIntoSplitPane(mMindMapComponent);
		}
	}

	public String getAdjustableProperty(final String label) {
		return resourceController.getProperty(label);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.main.FreeplaneMain#getContentPane()
	 */
	@Override
	public RootPaneContainer getRootPaneContainer() {
		return frame;
	}

	@Override
	public FreeplaneMenuBar getFreeplaneMenuBar() {
		return Controller.getCurrentModeController().getUserInputListenerFactory().getMenuBar();
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.main.FreeplaneMain#getJFrame()
	 */
	@Override
	public JFrame getJFrame() {
		return frame;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.main.FreeplaneMain#getLayeredPane()
	 */
	public JLayeredPane getLayeredPane() {
		return frame.getLayeredPane();
	}

	@Override
	public void insertComponentIntoSplitPane(final JComponent pMindMapComponent) {
		// --- Save the Component --
		mMindMapComponent = pMindMapComponent;
		// --- Devider position variables --
		int splitPanePosition = -1;
		int lastSplitPanePosition = -1;
		mapPane.setVisible(true);
		mSplitPane.setLeftComponent(null);
		mSplitPane.setRightComponent(null);
		if ("right".equals(mLocationPreferenceValue)) {
			mSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
			mSplitPane.setLeftComponent(mapPane);
			mSplitPane.setRightComponent(pMindMapComponent);
			splitPanePosition = resourceController.getIntProperty(SPLIT_PANE_RIGHT_POSITION, -1);
			lastSplitPanePosition = resourceController.getIntProperty(SPLIT_PANE_LAST_RIGHT_POSITION, -1);
		}
		else if ("left".equals(mLocationPreferenceValue)) {
			mSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
			mSplitPane.setLeftComponent(pMindMapComponent);
			mSplitPane.setRightComponent(mapPane);
			splitPanePosition = resourceController.getIntProperty(SPLIT_PANE_LEFT_POSITION, -1);
			lastSplitPanePosition = resourceController.getIntProperty(SPLIT_PANE_LAST_LEFT_POSITION, -1);
		}
		else if ("top".equals(mLocationPreferenceValue)) {
			mSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			mSplitPane.setLeftComponent(pMindMapComponent);
			mSplitPane.setRightComponent(mapPane);
			splitPanePosition = resourceController.getIntProperty(SPLIT_PANE_TOP_POSITION, -1);
			lastSplitPanePosition = resourceController.getIntProperty(SPLIT_PANE_LAST_TOP_POSITION, -1);
		}
		else if ("bottom".equals(mLocationPreferenceValue)) {
			mSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			mSplitPane.setLeftComponent(mapPane);
			mSplitPane.setRightComponent(pMindMapComponent);
			splitPanePosition = resourceController.getIntProperty(SPLIT_PANE_POSITION, -1);
			lastSplitPanePosition = resourceController.getIntProperty(SPLIT_PANE_LAST_POSITION, -1);
		}
		mSplitPane.setContinuousLayout(true);
		mSplitPane.setOneTouchExpandable(false);
		setSplitPaneLayoutManager();
		/*
		 * This means that the mind map area gets all the space that results
		 * from resizing the window.
		 */
		mSplitPane.setResizeWeight(1.0d);
		if (splitPanePosition != -1 && lastSplitPanePosition != -1) {
			mSplitPane.setDividerLocation(splitPanePosition);
			mSplitPane.setLastDividerLocation(lastSplitPanePosition);
		}
		else {
			mSplitPane.setDividerLocation(0.5);
		}
	}

	@Override
	public boolean isApplet() {
		return false;
	}

	@Override
	public void openDocument(final URI uri) throws IOException {
		String uriString = uri.toString();
		final String UNC_PREFIX = "file:////";
		if (uriString.startsWith(UNC_PREFIX)) {
			uriString = "file://" + uriString.substring(UNC_PREFIX.length());
		}
		final String osName = System.getProperty("os.name");
		if (osName.substring(0, 3).equals("Win")) {
			String propertyString = "default_browser_command_windows";
			if (osName.indexOf("9") != -1 || osName.indexOf("Me") != -1) {
				propertyString += "_9x";
			}
			else {
				propertyString += "_nt";
			}
			String[] command = null;
			try {
				final Object[] messageArguments = { uriString };
				final MessageFormat formatter = new MessageFormat(ResourceController.getResourceController()
				    .getProperty(propertyString));
				final String browserCommand = formatter.format(messageArguments);
				final String scheme = uri.getScheme();
                if (scheme.equals("file") || scheme.equals("smb")) {
                    if(scheme.equals("smb")){
                        uriString = Compat.smbUri2unc(uri);
                    }
					if (System.getProperty("os.name").startsWith("Windows 2000"))
						command = new String[] { "rundll32", "shell32.dll,ShellExec_RunDLL", uriString };
					else
	                    command = new String[] { "rundll32", "url.dll,FileProtocolHandler", uriString };
				}
				else if (uriString.startsWith("mailto:")) {
					command = new String[] { "rundll32", "url.dll,FileProtocolHandler", uriString };
				}
				else {
					Controller.exec(browserCommand);
					return;
				}
				Controller.exec(command);
			}
			catch (final IOException x) {
				UITools
				    .errorMessage("Could not invoke browser.\n\nFreeplane excecuted the following statement on a command line:\n\""
				            + command
				            + "\".\n\nYou may look at the user or default property called '"
				            + propertyString
				            + "'.");
				System.err.println("Caught: " + x);
			}
		}
		else if (osName.startsWith("Mac OS")) {
			String browserCommand = null;
			try {
				final Object[] messageArguments = { uriString, uriString };
				final MessageFormat formatter = new MessageFormat(ResourceController.getResourceController()
				    .getProperty("default_browser_command_mac"));
				browserCommand = formatter.format(messageArguments);
				Controller.exec(browserCommand);
			}
			catch (final IOException ex2) {
				UITools
				    .errorMessage("Could not invoke browser.\n\nFreeplane excecuted the following statement on a command line:\n\""
				            + browserCommand
				            + "\".\n\nYou may look at the user or default property called 'default_browser_command_mac'.");
				System.err.println("Caught: " + ex2);
			}
		}
		else {
			String browserCommand = null;
			try {
				final Object[] messageArguments = { uriString, uriString };
				final MessageFormat formatter = new MessageFormat(ResourceController.getResourceController()
				    .getProperty("default_browser_command_other_os"));
				browserCommand = formatter.format(messageArguments);
				Controller.exec(browserCommand);
			}
			catch (final IOException ex2) {
				UITools
				    .errorMessage("Could not invoke browser.\n\nFreeplane excecuted the following statement on a command line:\n\""
				            + browserCommand
				            + "\".\n\nYou may look at the user or default property called 'default_browser_command_other_os'.");
				System.err.println("Caught: " + ex2);
			}
		}
	}

	/**
	 * Open url in WWW browser. This method hides some differences between
	 * operating systems.
	 */
	@Override
	public void openDocument(final URL url) throws Exception {
		URI uri = null;
		try {
			uri = url.toURI();
		}
		catch (URISyntaxException e) {
			uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), url.getRef());
		}
		openDocument(uri);
	}

	@Override
	public boolean quit() {
		if (!super.quit()) {
			return false;
		}
		frame.dispose();
		return true;
	}

	@Override
	public void removeSplitPane() {
		saveSplitPanePosition();
		mMindMapComponent = null;
		mSplitPane.setLeftComponent(null);
		mSplitPane.setRightComponent(null);
		mSplitPane.setLeftComponent(mapPane);
		setSplitPaneLayoutManager();
		final Controller controller = Controller.getCurrentModeController().getController();
		final IMapSelection selection = controller.getSelection();
		if(selection == null){
			return;
		}
		final NodeModel node = selection.getSelected();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				final Component component = controller.getMapViewManager().getComponent(node);
				if (component != null) {
					component.requestFocus();
				}
			}
		});
	}

	private void setSplitPaneLayoutManager() {
	    final LayoutManager layout = mSplitPane.getLayout();
	    if(layout instanceof SplitPaneLayoutManagerDecorator){
	    	return;
	    }
		mSplitPane.setLayout(new SplitPaneLayoutManagerDecorator(layout));
    }

	@Override
	public void saveProperties() {
		saveSplitPanePosition();
		if (!isFullScreenEnabled()) {
			final int winState = frame.getExtendedState() & ~Frame.ICONIFIED;
			if (JFrame.MAXIMIZED_BOTH != (winState & JFrame.MAXIMIZED_BOTH)) {
				resourceController.setProperty("appwindow_x", String.valueOf(frame.getX()));
				resourceController.setProperty("appwindow_y", String.valueOf(frame.getY()));
				resourceController.setProperty("appwindow_width", String.valueOf(frame.getWidth()));
				resourceController.setProperty("appwindow_height", String.valueOf(frame.getHeight()));
			}
			resourceController.setProperty("appwindow_state", String.valueOf(winState));
		}
		mapViewWindows.saveLayout();
	}

	private void saveSplitPanePosition() {
		if (mSplitPane == null) {
			return;
		}
		if ("right".equals(mLocationPreferenceValue)) {
			resourceController.setProperty(SPLIT_PANE_RIGHT_POSITION, "" + mSplitPane.getDividerLocation());
			resourceController.setProperty(SPLIT_PANE_LAST_RIGHT_POSITION, "" + mSplitPane.getLastDividerLocation());
		}
		else if ("left".equals(mLocationPreferenceValue)) {
			resourceController.setProperty(SPLIT_PANE_LEFT_POSITION, "" + mSplitPane.getDividerLocation());
			resourceController.setProperty(SPLIT_PANE_LAST_LEFT_POSITION, "" + mSplitPane.getLastDividerLocation());
		}
		else if ("top".equals(mLocationPreferenceValue)) {
			resourceController.setProperty(SPLIT_PANE_TOP_POSITION, "" + mSplitPane.getDividerLocation());
			resourceController.setProperty(SPLIT_PANE_LAST_TOP_POSITION, "" + mSplitPane.getLastDividerLocation());
		}
		else { // "bottom".equals(mLocationPreferenceValue) also covered
			resourceController.setProperty(SPLIT_PANE_POSITION, "" + mSplitPane.getDividerLocation());
			resourceController.setProperty(SPLIT_PANE_LAST_POSITION, "" + mSplitPane.getLastDividerLocation());
		}
	}

	@Override
	protected void setFreeplaneMenuBar(final FreeplaneMenuBar menuBar) {
		frame.setJMenuBar(menuBar);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.main.FreeplaneMain#setTitle(java.lang.String)
	 */
	@Override
	public void setTitle(final String frameTitle) {
		frame.setTitle(frameTitle);
		mapViewWindows.setTitle();
	}

	@Override
	public void setWaitingCursor(final boolean waiting) {
		if (waiting) {
			frame.getRootPane().getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			frame.getRootPane().getGlassPane().setVisible(true);
		}
		else {
			frame.getRootPane().getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			frame.getRootPane().getGlassPane().setVisible(false);
		}
	}

	@Override
    public void viewNumberChanged(final int number) {
		navigationPreviousMap.setEnabled(number > 1);
		navigationNextMap.setEnabled(number > 1);
	}

	public void initFrame(final JFrame frame) {
		// Preserve the existing icon image under Mac OS X
		if (!Compat.isMacOsX()) {
			final ImageIcon mWindowIcon;
			if (Compat.isLowerJdk(Compat.VERSION_1_6_0)) {
				mWindowIcon = new ImageIcon(ResourceController.getResourceController().getResource(
				    "/images/Freeplane_frame_icon.png"));
			}
			else {
				mWindowIcon = new ImageIcon(ResourceController.getResourceController().getResource(
				    "/images/Freeplane_frame_icon_64x64.png"));
			}
			frame.setIconImage(mWindowIcon.getImage());
		}
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				Controller.getCurrentController().quit(new ActionEvent(this, 0, "quit"));
			}
			/*
			 * fc, 14.3.2008: Completely removed, as it damaged the focus if for
			 * example the note window was active.
			 */
		});
		frame.setFocusTraversalKeysEnabled(false);
		final int win_width = ResourceController.getResourceController().getIntProperty("appwindow_width", -1);
		final int win_height = ResourceController.getResourceController().getIntProperty("appwindow_height", -1);
		final int win_x = ResourceController.getResourceController().getIntProperty("appwindow_x", -1);
		final int win_y = ResourceController.getResourceController().getIntProperty("appwindow_y", -1);
		UITools.setBounds(frame, win_x, win_y, win_width, win_height);
		setFrameSize(frame.getBounds());
		
		applyFrameSize(frame, win_x, win_y);
		
		int win_state = Integer
		    .parseInt(ResourceController.getResourceController().getProperty("appwindow_state", "0"));
		win_state = ((win_state & Frame.ICONIFIED) != 0) ? Frame.NORMAL : win_state;
		frame.setExtendedState(win_state);
	}
 
	private void applyFrameSize(final JFrame frame, int win_x, int win_y) {
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle r = env.getMaximumWindowBounds();
		for(GraphicsDevice device : env.getScreenDevices()) {
			if(!device.equals(env.getDefaultScreenDevice())) {
				Rectangle bounds = device.getDefaultConfiguration().getBounds();
				r.add(bounds);
			}
		}
		frame.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
		frame.setPreferredSize(new Dimension(Math.min(r.width, frame.getBounds().width), Math.min(r.height, frame.getBounds().height)));
//		frame.setLocation(Math.max(r.x, frame.getBounds().x), Math.max(r.y, frame.getBounds().y));
		frame.setLocation(Math.max(r.x, win_x), Math.max(r.y, win_y));
	}

	public void openMapsOnStart() {
	    mapViewWindows.loadLayout();
    }

	public void focusTo(MapView currentMapView) {
	    mapViewWindows.focusMapViewLater(currentMapView);

    }

	public void previousMapView() {
		mapViewWindows.selectPreviousMapView();
		
	}

	public void nextMapView() {
		mapViewWindows.selectNextMapView();
	}

}
