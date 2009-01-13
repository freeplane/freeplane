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
package org.freeplane.core.frame;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;

import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.util.Tools;

public class ApplicationViewController extends ViewController {
	private static final String SPLIT_PANE_LAST_POSITION = "split_pane_last_position";
	private static final String SPLIT_PANE_POSITION = "split_pane_position";
	final private JFrame frame;
	private MapViewTabs mapViewManager;
	private JComponent mContentComponent = null;
	private JSplitPane mSplitPane;
	final private ResourceController resourceController;

	public ApplicationViewController(final IMapViewManager mapViewController) {
		super(mapViewController);
		resourceController = Controller.getResourceController();
		frame = new JFrame("Freeplane");
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
	public void init() {
		final ImageIcon mWindowIcon = new ImageIcon(resourceController
		    .getResource("/images/Freeplane_frame_icon.png"));
		getJFrame().setIconImage(mWindowIcon.getImage());
		getContentPane().setLayout(new BorderLayout());
		super.init();
		if (Controller.getResourceController().getBoolProperty("no_scrollbar")) {
			getScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			getScrollPane().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		else {
			getScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			getScrollPane().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		}
		mContentComponent = getScrollPane();
		final boolean shouldUseTabbedPane = Controller.getResourceController().getBoolProperty(
		    ResourceController.RESOURCES_USE_TABBED_PANE);
		if (shouldUseTabbedPane) {
			mapViewManager = new MapViewTabs(this, mContentComponent);
		}
		else {
			getContentPane().add(mContentComponent, BorderLayout.CENTER);
		}
		getContentPane().add(getStatusLabel(), BorderLayout.SOUTH);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				Controller.getController().quit(new ActionEvent(this, 0, "quit"));
			}
			/*
			 * fc, 14.3.2008: Completely removed, as it damaged the focus if for
			 * example the note window was active.
			 */
		});
		if (Tools.safeEquals(Controller.getResourceController().getProperty("toolbarVisible"),
		    "false")) {
			Controller.getController().getViewController().setToolbarVisible(false);
		}
		if (Tools.safeEquals(Controller.getResourceController().getProperty("leftToolbarVisible"),
		    "false")) {
			Controller.getController().getViewController().setLeftToolbarVisible(false);
		}
		frame.setFocusTraversalKeysEnabled(false);
		frame.pack();
		int win_width = Controller.getResourceController().getIntProperty("appwindow_width", 0);
		int win_height = Controller.getResourceController().getIntProperty("appwindow_height", 0);
		int win_x = Controller.getResourceController().getIntProperty("appwindow_x", 0);
		int win_y = Controller.getResourceController().getIntProperty("appwindow_y", 0);
		win_width = (win_width > 0) ? win_width : 640;
		win_height = (win_height > 0) ? win_height : 440;
		final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
		final Insets screenInsets = defaultToolkit
		    .getScreenInsets(frame.getGraphicsConfiguration());
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
		int win_state = Integer.parseInt(Controller.getResourceController().getProperty(
		    "appwindow_state", "0"));
		win_state = ((win_state & Frame.ICONIFIED) != 0) ? Frame.NORMAL : win_state;
		frame.setExtendedState(win_state);
	}

	@Override
	public JSplitPane insertComponentIntoSplitPane(final JComponent pMindMapComponent) {
		if (mSplitPane != null) {
			return mSplitPane;
		}
		removeContentComponent();
		mSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, getScrollPane(), pMindMapComponent);
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
		final int splitPanePosition = Controller.getResourceController().getIntProperty(
		    ApplicationViewController.SPLIT_PANE_POSITION, -1);
		final int lastSplitPanePosition = Controller.getResourceController().getIntProperty(
		    ApplicationViewController.SPLIT_PANE_LAST_POSITION, -1);
		if (splitPanePosition != -1 && lastSplitPanePosition != -1) {
			mSplitPane.setDividerLocation(splitPanePosition);
			mSplitPane.setLastDividerLocation(lastSplitPanePosition);
		}
		return mSplitPane;
	}

	@Override
	public boolean isApplet() {
		return false;
	}

	/**
	 * Open url in WWW browser. This method hides some differences between
	 * operating systems.
	 */
	@Override
	public void openDocument(final URL url) throws Exception {
		String correctedUrl = new String(url.toExternalForm());
		if (url.getProtocol().equals("file")) {
			correctedUrl = correctedUrl.replace('\\', '/').replaceAll(" ", "%20");
		}
		final String osName = System.getProperty("os.name");
		if (osName.substring(0, 3).equals("Win")) {
			String propertyString = new String("default_browser_command_windows");
			if (osName.indexOf("9") != -1 || osName.indexOf("Me") != -1) {
				propertyString += "_9x";
			}
			else {
				propertyString += "_nt";
			}
			String browser_command = new String();
			String command = new String();
			try {
				final Object[] messageArguments = { url.toString() };
				final MessageFormat formatter = new MessageFormat(Controller
				    .getResourceController().getProperty(propertyString));
				browser_command = formatter.format(messageArguments);
				if (url.getProtocol().equals("file")) {
					command = "rundll32 url.dll,FileProtocolHandler " + url.toString();
					if (System.getProperty("os.name").startsWith("Windows 2000")) {
						command = "rundll32 shell32.dll,ShellExec_RunDLL " + url.toString();
					}
				}
				else if (url.toString().startsWith("mailto:")) {
					command = "rundll32 url.dll,FileProtocolHandler " + url.toString();
				}
				else {
					command = browser_command;
				}
				Runtime.getRuntime().exec(command);
			}
			catch (final IOException x) {
				Controller.getController().errorMessage(
				    "Could not invoke browser.\n\nFreeplane excecuted the following statement on a command line:\n\""
				            + command
				            + "\".\n\nYou may look at the user or default property called '"
				            + propertyString + "'.");
				System.err.println("Caught: " + x);
			}
		}
		else if (osName.startsWith("Mac OS")) {
			String browser_command = new String();
			try {
				final Object[] messageArguments = { correctedUrl, url.toString() };
				final MessageFormat formatter = new MessageFormat(Controller
				    .getResourceController().getProperty("default_browser_command_mac"));
				browser_command = formatter.format(messageArguments);
				Runtime.getRuntime().exec(browser_command);
			}
			catch (final IOException ex2) {
				Controller
				    .getController()
				    .errorMessage(
				        "Could not invoke browser.\n\nFreeplane excecuted the following statement on a command line:\n\""
				                + browser_command
				                + "\".\n\nYou may look at the user or default property called 'default_browser_command_mac'.");
				System.err.println("Caught: " + ex2);
			}
		}
		else {
			String browser_command = new String();
			try {
				final Object[] messageArguments = { correctedUrl, url.toString() };
				final MessageFormat formatter = new MessageFormat(Controller
				    .getResourceController().getProperty("default_browser_command_other_os"));
				browser_command = formatter.format(messageArguments);
				Runtime.getRuntime().exec(browser_command);
			}
			catch (final IOException ex2) {
				Controller
				    .getController()
				    .errorMessage(
				        "Could not invoke browser.\n\nFreeplane excecuted the following statement on a command line:\n\""
				                + browser_command
				                + "\".\n\nYou may look at the user or default property called 'default_browser_command_other_os'.");
				System.err.println("Caught: " + ex2);
			}
		}
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
		if (mSplitPane != null) {
			resourceController.setProperty(ApplicationViewController.SPLIT_PANE_POSITION, ""
			        + mSplitPane.getDividerLocation());
			resourceController.setProperty(ApplicationViewController.SPLIT_PANE_LAST_POSITION, ""
			        + mSplitPane.getLastDividerLocation());
			removeContentComponent();
			mContentComponent = getScrollPane();
			setContentComponent();
			mSplitPane = null;
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
	void setFreeplaneMenuBar(final FreeplaneMenuBar menuBar) {
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
			frame.getRootPane().getGlassPane().setCursor(
			    Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			frame.getRootPane().getGlassPane().setVisible(true);
		}
		else {
			frame.getRootPane().getGlassPane().setCursor(
			    Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			frame.getRootPane().getGlassPane().setVisible(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.main.FreeplaneMain#exit()
	 */
	@Override
	public void stop() {
		final int winState = frame.getExtendedState();
		if (JFrame.MAXIMIZED_BOTH != (winState & JFrame.MAXIMIZED_BOTH)) {
			resourceController.setProperty("appwindow_x", String.valueOf(frame.getX()));
			resourceController.setProperty("appwindow_y", String.valueOf(frame.getY()));
			resourceController.setProperty("appwindow_width", String.valueOf(frame.getWidth()));
			resourceController.setProperty("appwindow_height", String.valueOf(frame.getHeight()));
		}
		resourceController.setProperty("appwindow_state", String.valueOf(winState));
		resourceController.saveProperties();
		frame.dispose();
	}
}
