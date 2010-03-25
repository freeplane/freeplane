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
import java.awt.Container;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.IMapViewManager;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.components.LimitedWidthTooltipUI;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;

class ApplicationViewController extends ViewController {
	private static final String TOOL_TIP_MANAGER = "toolTipManager.";
	private static final String TOOL_TIP_MANAGER_DISMISS_DELAY = "toolTipManager.dismissDelay";
	private static final String TOOL_TIP_MANAGER_INITIAL_DELAY = "toolTipManager.initialDelay";
	private static final String TOOL_TIP_MANAGER_RESHOW_DELAY = "toolTipManager.reshowDelay";

	public static final String RESOURCES_USE_TABBED_PANE = "use_tabbed_pane";
	private static final String SPLIT_PANE_LAST_LEFT_POSITION = "split_pane_last_left_position";
	private static final String SPLIT_PANE_LAST_POSITION = "split_pane_last_position";
	private static final String SPLIT_PANE_LAST_RIGHT_POSITION = "split_pane_last_right_position";
	private static final String SPLIT_PANE_LAST_TOP_POSITION = "split_pane_last_top_position";
	private static final String SPLIT_PANE_LEFT_POSITION = "split_pane_left_position";
	private static final String SPLIT_PANE_POSITION = "split_pane_position";
	private static final String SPLIT_PANE_RIGHT_POSITION = "split_pane_right_position";
	private static final String SPLIT_PANE_TOP_POSITION = "split_pane_top_position";
	final private Controller controller;
	final private JFrame frame;
	private MapViewTabs mapViewManager;
	private JComponent mContentComponent = null;
	/** Contains the value where the Note Window should be displayed (right, left, top, bottom) */
	private String mLocationPreferenceValue;
	/** Contains the Note Window Component */
	private JComponent mMindMapComponent;
	private JSplitPane mSplitPane;
	final private NavigationNextMapAction navigationNextMap;
	final private NavigationPreviousMapAction navigationPreviousMap;
	final private ResourceController resourceController;

	public ApplicationViewController(final Controller controller, final IMapViewManager mapViewController,
	                                 final JFrame frame) {
		super(controller, mapViewController);
		this.controller = controller;
		navigationPreviousMap = new NavigationPreviousMapAction(controller);
		controller.addAction(navigationPreviousMap);
		navigationNextMap = new NavigationNextMapAction(controller);
		controller.addAction(navigationNextMap);
		resourceController = ResourceController.getResourceController();
		this.frame = frame;
		getContentPane().setLayout(new BorderLayout());
		// --- Set Note Window Location ---
		mLocationPreferenceValue = resourceController.getProperty("note_location", "bottom");
		if (ResourceController.getResourceController().getBooleanProperty("no_scrollbar")) {
			getScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			getScrollPane().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		else {
			getScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			getScrollPane().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		}
		mContentComponent = getScrollPane();
		final boolean shouldUseTabbedPane = ResourceController.getResourceController().getBooleanProperty(
		    ApplicationViewController.RESOURCES_USE_TABBED_PANE);
		if (shouldUseTabbedPane) {
			mapViewManager = new MapViewTabs(controller, this, mContentComponent);
		}
		else {
			getContentPane().add(mContentComponent, BorderLayout.CENTER);
		}
		getContentPane().add(getStatusBar(), BorderLayout.SOUTH);
		initFrame(frame);
	}

	/**
	 * Called from the Controller, when the Location of the Note Window is changed on the Menu->View->Note Window Location 
	 */
	@Override
	public void changeNoteWindowLocation(final boolean isSplitWindowOnorOff) {
		// -- Remove Note Window from old location -- 
		if (isSplitWindowOnorOff == true) {
			// --- Remove and put it back in the new location the Note Window --
			removeSplitPane();
		}
		// --- Get the new location --
		mLocationPreferenceValue = resourceController.getProperty("note_location");
		// -- Display Note Window in the new location --
		if (isSplitWindowOnorOff == true) {
			// --- Place the Note Window in the new place --
			insertComponentIntoSplitPane(mMindMapComponent);
		}
	}

	public String getAdjustableProperty(final String label) {
		return resourceController.getAdjustableProperty(label);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.main.FreeplaneMain#getContentPane()
	 */
	@Override
	public Container getContentPane() {
		return frame.getContentPane();
	}

	@Override
	public FreeplaneMenuBar getFreeplaneMenuBar() {
		return (FreeplaneMenuBar) frame.getJMenuBar();
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
	public JSplitPane insertComponentIntoSplitPane(final JComponent pMindMapComponent) {
		if (mSplitPane != null) {
			return mSplitPane;
		}
		removeContentComponent();
		// --- Save the Component --
		mMindMapComponent = pMindMapComponent;
		// --- Devider position variables --
		int splitPanePosition = -1;
		int lastSplitPanePosition = -1;
		if ("right".equals(mLocationPreferenceValue)) {
			mSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getScrollPane(), pMindMapComponent);
			splitPanePosition = resourceController.getIntProperty(SPLIT_PANE_RIGHT_POSITION, -1);
			lastSplitPanePosition = resourceController.getIntProperty(SPLIT_PANE_LAST_RIGHT_POSITION, -1);
		}
		else if ("left".equals(mLocationPreferenceValue)) {
			mSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pMindMapComponent, getScrollPane());
			splitPanePosition = resourceController.getIntProperty(SPLIT_PANE_LEFT_POSITION, -1);
			lastSplitPanePosition = resourceController.getIntProperty(SPLIT_PANE_LAST_LEFT_POSITION, -1);
		}
		else if ("top".equals(mLocationPreferenceValue)) {
			mSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pMindMapComponent, getScrollPane());
			splitPanePosition = resourceController.getIntProperty(SPLIT_PANE_TOP_POSITION, -1);
			lastSplitPanePosition = resourceController.getIntProperty(SPLIT_PANE_LAST_TOP_POSITION, -1);
		}
		else if ("bottom".equals(mLocationPreferenceValue)) {
			mSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, getScrollPane(), pMindMapComponent);
			splitPanePosition = resourceController.getIntProperty(SPLIT_PANE_POSITION, -1);
			lastSplitPanePosition = resourceController.getIntProperty(SPLIT_PANE_LAST_POSITION, -1);
		}
		mSplitPane.setContinuousLayout(true);
		mSplitPane.setOneTouchExpandable(false);
		/*
		 * This means that the mind map area gets all the space that results
		 * from resizing the window.
		 */
		mSplitPane.setResizeWeight(1.0d);
		final InputMap map = (InputMap) UIManager.get("SplitPane.ancestorInputMap");
		final KeyStroke keyStrokeF6 = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
		final KeyStroke keyStrokeF8 = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
		map.remove(keyStrokeF6);
		map.remove(keyStrokeF8);
		mContentComponent = mSplitPane;
		setContentComponent();
		if (splitPanePosition != -1 && lastSplitPanePosition != -1) {
			mSplitPane.setDividerLocation(splitPanePosition);
			mSplitPane.setLastDividerLocation(lastSplitPanePosition);
		}
		else {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					mSplitPane.setDividerLocation(0.5);
				}
			});
		}
		return mSplitPane;
	}

	@Override
	public boolean isApplet() {
		return false;
	}

	@Override
	public void openDocument(final URI uri) throws IOException {
		String uriString = uri.toString();
		final String UNC_PREFIX = "file:////";
		if(uriString.startsWith(UNC_PREFIX)){
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
				String browserCommand = formatter.format(messageArguments);
				if (uri.getScheme().equals("file")) {
					command = new String[]{"rundll32", "url.dll,FileProtocolHandler", uriString};
					if (System.getProperty("os.name").startsWith("Windows 2000")) {
						command = new String[]{"rundll32", "shell32.dll,ShellExec_RunDLL",  uriString};
					}
				}
				else if (uriString.startsWith("mailto:")) {
					command = new String[]{"rundll32", "url.dll,FileProtocolHandler", uriString};
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
				final Object[] messageArguments = { uriString, uriString};
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
		final URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), url.getRef());
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

	private void removeContentComponent() {
		if (mapViewManager != null) {
			mapViewManager.removeContentComponent();
		}
		else {
			getContentPane().remove(mContentComponent);
			frame.getRootPane().revalidate();
		}
	}

	@Override
	public void removeSplitPane() {
		if (mSplitPane == null) {
			return;
		}
		saveSplitPanePosition();
		removeContentComponent();
		mContentComponent = getScrollPane();
		setContentComponent();
		mSplitPane = null;
	}

	@Override
	public void saveProperties() {
		saveSplitPanePosition();
		resourceController.setProperty("map_view_zoom", Float.toString(getZoom()));
		if(! isFullScreenEnabled()){
			final int winState = frame.getExtendedState() & ~Frame.ICONIFIED;
			if (JFrame.MAXIMIZED_BOTH != (winState & JFrame.MAXIMIZED_BOTH)) {
				resourceController.setProperty("appwindow_x", String.valueOf(frame.getX()));
				resourceController.setProperty("appwindow_y", String.valueOf(frame.getY()));
				resourceController.setProperty("appwindow_width", String.valueOf(frame.getWidth()));
				resourceController.setProperty("appwindow_height", String.valueOf(frame.getHeight()));
			}
			resourceController.setProperty("appwindow_state", String.valueOf(winState));
		}
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

	private void setContentComponent() {
		if (mapViewManager != null) {
			mapViewManager.setContentComponent(mContentComponent);
		}
		else {
			getContentPane().add(mContentComponent, BorderLayout.CENTER);
			frame.getRootPane().revalidate();
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
	public void setTitle(final String title) {
		frame.setTitle(title);
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
	protected void viewNumberChanged(final int number) {
		navigationPreviousMap.setEnabled(number > 0);
		navigationNextMap.setEnabled(number > 0);
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
						"/images/Freeplane_frame_icon_32x32.png"));
			}
			frame.setIconImage(mWindowIcon.getImage());
		}

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
		frame.setFocusTraversalKeysEnabled(false);
		final int win_width = ResourceController.getResourceController().getIntProperty("appwindow_width", 0);
		final int win_height = ResourceController.getResourceController().getIntProperty("appwindow_height", 0);
		final int win_x = ResourceController.getResourceController().getIntProperty("appwindow_x", 0);
		final int win_y = ResourceController.getResourceController().getIntProperty("appwindow_y", 0);
		UITools.setBounds(frame, win_x, win_y, win_width, win_height);
		setFrameSize(frame.getBounds());
		int win_state = Integer
		    .parseInt(ResourceController.getResourceController().getProperty("appwindow_state", "0"));
		win_state = ((win_state & Frame.ICONIFIED) != 0) ? Frame.NORMAL : win_state;
		frame.setExtendedState(win_state);
		setTooltipDelays();
		LimitedWidthTooltipUI.initialize();
		ResourceController.getResourceController().addPropertyChangeListener(new IFreeplanePropertyListener() {
			public void propertyChanged(final String propertyName, final String newValue, final String oldValue) {
				if (propertyName.startsWith(TOOL_TIP_MANAGER)) {
					setTooltipDelays();
				}
			}
		});
	}

	private void setTooltipDelays() {
		final ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
		final int initialDelay = ResourceController.getResourceController().getIntProperty(
		    TOOL_TIP_MANAGER_INITIAL_DELAY, 0);
		toolTipManager.setInitialDelay(initialDelay);
		final int dismissDelay = ResourceController.getResourceController().getIntProperty(
		    TOOL_TIP_MANAGER_DISMISS_DELAY, 0);
		toolTipManager.setDismissDelay(dismissDelay);
		final int reshowDelay = ResourceController.getResourceController().getIntProperty(
		    TOOL_TIP_MANAGER_RESHOW_DELAY, 0);
		toolTipManager.setReshowDelay(reshowDelay);
		final int maxWidth = ResourceController.getResourceController().getIntProperty(
		    "toolTipManager.max_tooltip_width", Integer.MAX_VALUE);
		LimitedWidthTooltipUI.setMaximumWidth(maxWidth);
	}

}
