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
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.Hyperlink;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.FrameController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.overview.BookmarkToolbarPane;

class ApplicationViewController extends FrameController {
	private static final String APPWINDOW_WIDTH = "appwindow_width";
	private static final String APPWINDOW_HEIGHT = "appwindow_height";
	private static final String APPWINDOW_X = "appwindow_x";
	private static final String APPWINDOW_Y = "appwindow_y";
	private static final String APPWINDOW_STATE = "appwindow_state";
	private static final String X11_TOOLKIT_CLASS_NAME = "sun.awt.X11.XToolkit";

	static boolean needsMaximizedFrameResynchronization(final boolean isX11Toolkit, final int windowState,
			final Rectangle frameBounds, final Rectangle screenBounds) {
		return isX11Toolkit && (windowState & Frame.ICONIFIED) == 0
				&& (windowState & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH
				&& frameBounds != null && screenBounds != null
				&& (frameBounds.x != screenBounds.x || frameBounds.y != screenBounds.y);
	}

	private static boolean isX11Toolkit() {
		return X11_TOOLKIT_CLASS_NAME.equals(Toolkit.getDefaultToolkit().getClass().getName());
	}

	private static void resynchronizeMaximizedFrame(final JFrame frame, final int windowState) {
		final GraphicsConfiguration graphicsConfiguration = frame.getGraphicsConfiguration();
		if (graphicsConfiguration == null) {
			return;
		}
		final Rectangle screenBounds = graphicsConfiguration.getBounds();
		if (needsMaximizedFrameResynchronization(isX11Toolkit(), windowState, frame.getBounds(), screenBounds)) {
			frame.setBounds(screenBounds);
			frame.validate();
		}
	}

	private static Image frameIcon(String size) {
        return new ImageIcon(ResourceController.getResourceController().getResource(
                "/images/Freeplane_frame_icon_"+ size + ".png")).getImage();
    }

	// // 	final private Controller controller;
	final private JFrame mainFrame;
	final private NavigationNextMapAction navigationNextMap;
	final private NavigationPreviousMapAction navigationPreviousMap;
	private MapViewDockingWindows mapViewWindows;
	private final java.util.Map<Window, BookmarkToolbarPane> bookmarkToolbarPanes = new java.util.HashMap<>();
	private final FrameComponentMover frameComponentMover;

	static Rectangle getStoredFrameBounds(final Component frame) {
		final ResourceController resourceController = ResourceController.getResourceController();
		final int winWidth = resourceController.getIntProperty(APPWINDOW_WIDTH, -1);
		final int winHeight = resourceController.getIntProperty(APPWINDOW_HEIGHT, -1);
		final int winX = resourceController.getIntProperty(APPWINDOW_X, -1);
		final int winY = resourceController.getIntProperty(APPWINDOW_Y, -1);
		return UITools.getValidFrameBounds(frame, winX, winY, winWidth, winHeight);
	}

	static GraphicsConfiguration getStoredFrameGraphicsConfiguration() {
		final ResourceController resourceController = ResourceController.getResourceController();
		final int winX = resourceController.getIntProperty(APPWINDOW_X, -1);
		final int winY = resourceController.getIntProperty(APPWINDOW_Y, -1);
		if (winX == -1 || winY == -1) {
			return null;
		}
		final GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		for (final GraphicsDevice graphicsDevice : graphicsEnvironment.getScreenDevices()) {
			for (final GraphicsConfiguration graphicsConfiguration : graphicsDevice.getConfigurations()) {
				if (graphicsConfiguration.getBounds().contains(winX, winY)) {
					return graphicsConfiguration;
				}
			}
		}
		return null;
	}

    public ApplicationViewController( Controller controller, final IMapViewManager mapViewController,
	                                 final JFrame frame, FrameComponentMover frameComponentMover) {
		super(controller, mapViewController, "");
//		this.controller = controller;
		navigationPreviousMap = new NavigationPreviousMapAction();
		controller.addAction(navigationPreviousMap);
		navigationNextMap = new NavigationNextMapAction();
		controller.addAction(navigationNextMap);
		controller.addAction(new NavigationMapNextViewAction());
		controller.addAction(new NavigationMapPreviousViewAction());
		this.mainFrame = frame;
		this.frameComponentMover = frameComponentMover;
	}

	/**
	 * Called from the Controller, when the Location of the Note Window is changed on the Menu->View->Note Window Location
	 */
	@Override
	public void changeNoteWindowLocation(String location) {
		final AuxiliarySplitPanes splitPanes = getSplitPanes();
		if (splitPanes != null) {
			splitPanes.changeAuxComponentSide(0, location);
		}
	}

	@Override
	public FreeplaneMenuBar getFreeplaneMenuBar() {
		return Controller.getCurrentModeController().getUserInputListenerFactory().getMenuBar();
	}

	@Override
	public void insertComponentIntoSplitPane(final JComponent pMindMapComponent) {
		final AuxiliarySplitPanes splitPanes = getSplitPanes();
		if (splitPanes != null) {
			splitPanes.insertComponentIntoSplitPane(0, pMindMapComponent, Controller.getCurrentModeController().getModeName());
		}
	}

	private AuxillaryEditorSplitPane getSplitPane() {
		final Component currentRootComponent = getCurrentRootComponent();
		if(currentRootComponent instanceof RootPaneContainer)
			return getSplitPane((RootPaneContainer) currentRootComponent);
		else
			return null;
	}

	/**
	 * Gets the AuxiliarySplitPanes manager for note operations.
	 */
	private AuxiliarySplitPanes getSplitPanes() {
		AuxillaryEditorSplitPane splitPane = getSplitPane();
		return splitPane != null ? splitPane.getManager() : null;
	}

	public AuxillaryEditorSplitPane getSplitPane(final RootPaneContainer topLevelAncestor) {
		final Container contentPane = ((JFrame) topLevelAncestor).getContentPane();
		final Component centerComponent = ((BorderLayout) contentPane.getLayout()).getLayoutComponent(BorderLayout.CENTER);
		if (centerComponent instanceof AuxillaryEditorSplitPane) {
			return (AuxillaryEditorSplitPane) centerComponent;
		}
		return null;
	}

	void createAuxillaryPaneForFloatingWindow(Window frame, Component rootWindow) {
		if (frame instanceof JFrame) {
			JFrame jFrame = (JFrame) frame;
			Container contentPane = jFrame.getContentPane();
			Component centralComponent = null;
			if (contentPane.getLayout() instanceof BorderLayout) {
				centralComponent = ((BorderLayout) contentPane.getLayout()).getLayoutComponent(BorderLayout.CENTER);
			}

			BookmarkToolbarPane bookmarkToolbarPane = new BookmarkToolbarPane(rootWindow);
			AuxiliarySplitPanes splitPanes = new AuxiliarySplitPanes(bookmarkToolbarPane);
			AuxillaryEditorSplitPane splitPane = splitPanes.getRootPane();
			bookmarkToolbarPanes.put(frame, bookmarkToolbarPane);

			if (centralComponent != null) {
				contentPane.remove(centralComponent);
			}
			contentPane.setLayout(new BorderLayoutWithVisibleCenterComponent());
			contentPane.add(splitPane, BorderLayout.CENTER);

			insertActiveComponentsIntoSplitPane(splitPane, frame);
		}
	}

	private void insertActiveComponentsIntoSplitPane(AuxillaryEditorSplitPane splitPane, Window frame) {
		// Do not create new components - let FrameComponentMover handle moving the existing component
	}


	void removeAuxillaryPaneForFloatingWindow(Window frame) {
		BookmarkToolbarPane bookmarkToolbarPane = bookmarkToolbarPanes.remove(frame);
		if (bookmarkToolbarPane != null) {
			bookmarkToolbarPane.dispose();
		}
	}

	@Override
	public void openDocument(final Hyperlink link) throws IOException {
		new Browser().openDocument(link);
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
		openDocument(new Hyperlink(uri));
	}

	@Override
	public boolean quit() {
		if (!super.quit()) {
			return false;
		}
		controller.fireApplicationStopped();
		mainFrame.dispose();
		return true;
	}

	@Override
	public void removeAuxiliaryComponent() {
		final AuxiliarySplitPanes splitPanes = getSplitPanes();
		if (splitPanes != null) {
			splitPanes.removeAuxiliaryComponent(0);
		}
	}


	@Override
	public void saveProperties() {
		if(mapViewWindows == null)
			return;
		final ApplicationResourceController resourceController = (ApplicationResourceController)ResourceController.getResourceController();
		if (mainFrame.isResizable()) {
			final int winState = mainFrame.getExtendedState() & ~Frame.ICONIFIED;
			if (Frame.MAXIMIZED_BOTH != (winState & Frame.MAXIMIZED_BOTH)) {
				resourceController.setProperty(APPWINDOW_X, String.valueOf(mainFrame.getX()));
				resourceController.setProperty(APPWINDOW_Y, String.valueOf(mainFrame.getY()));
				resourceController.setProperty(APPWINDOW_WIDTH, String.valueOf(mainFrame.getWidth()));
				resourceController.setProperty(APPWINDOW_HEIGHT, String.valueOf(mainFrame.getHeight()));
			}
			resourceController.setProperty(APPWINDOW_STATE, String.valueOf(winState));
		}
		mapViewWindows.saveLayout();
		resourceController.getLastOpenedList().saveProperties();
	}

	@Override
	protected void setFreeplaneMenuBar(final FreeplaneMenuBar menuBar) {
	    final JFrame menuComponent = getMenuComponent();
		if(! menuComponent.isVisible()) {
			return;
		}
		if(Compat.isMacOsX()) {
	        System.setProperty("apple.laf.useScreenMenuBar", "true");
            menuComponent.setJMenuBar(menuBar);
            System.setProperty("apple.laf.useScreenMenuBar", "false");
        }
	    else
	    	menuComponent.setJMenuBar(menuBar);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.main.FreeplaneMain#setTitle(java.lang.String)
	 */
	@Override
	public void setTitle(final String frameTitle) {
		mainFrame.setTitle(frameTitle);
		mapViewWindows.setTitle();
	}

	@Override
	public void setWaitingCursor(final boolean waiting) {
		if (waiting) {
			mainFrame.getRootPane().getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			mainFrame.getRootPane().getGlassPane().setVisible(true);
		}
		else {
			mainFrame.getRootPane().getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			mainFrame.getRootPane().getGlassPane().setVisible(false);
		}
	}

	@Override
    public void viewNumberChanged(final int number) {
		navigationPreviousMap.setEnabled(number > 1);
		navigationNextMap.setEnabled(number > 1);
	}



	@Override
	public void init(Controller controller) {
		mainFrame.getContentPane().setLayout(new BorderLayout());
		// --- Set Note Window Location ---
		// disable all hotkeys for JSplitPane
		mapViewWindows = new MapViewDockingWindows(this);
		final BookmarkToolbarPane mainBookmarkToolbarPane = new BookmarkToolbarPane(mapViewWindows.getRootWindow());
		AuxiliarySplitPanes splitPanes = new AuxiliarySplitPanes(mainBookmarkToolbarPane);
		AuxillaryEditorSplitPane splitPane = splitPanes.getRootPane();
		splitPane.setResizeWeight(1.0d);
		Container contentPane = mainFrame.getContentPane();
		contentPane.setLayout(new BorderLayoutWithVisibleCenterComponent());
        contentPane.add(splitPane, BorderLayout.CENTER);
		initFrame(mainFrame);
		super.init(controller);
	}

	private void initFrame(final JFrame frame) {
		// Preserve the existing icon image under Mac OS X
		if (!Compat.isMacOsX()) {
			    frame.setIconImages(Arrays.asList(
                        frameIcon("16x16"),
                        frameIcon("32x32"),
                        frameIcon("64x64"),
                        frameIcon("128x128"),
                        frameIcon("256x256"),
                        frameIcon("512x512")
			            ));
		}
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				Controller.getCurrentController().quit();
			}
			/*
			 * fc, 14.3.2008: Completely removed, as it damaged the focus if for
			 * example the note window was active.
			 */
		});
		frame.setFocusTraversalKeysEnabled(false);
		installMaximizedFrameResynchronization(frame);
        frame.setBounds(getStoredFrameBounds(frame));
		frame.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));


		int win_state = Integer
		    .parseInt(ResourceController.getResourceController().getProperty(APPWINDOW_STATE, "0"));
		win_state = ((win_state & Frame.ICONIFIED) != 0) ? Frame.NORMAL : win_state;
		if (!Compat.isMacOsX() || (win_state & Frame.MAXIMIZED_BOTH) == 0) {
			frame.setExtendedState(win_state);
		}

		// Register full screen listener for macOS
		Compat.registerFullScreenListener(frame);
	}

	private void installMaximizedFrameResynchronization(final JFrame frame) {
		final WindowAdapter frameResynchronizer = new WindowAdapter() {
			@Override
			public void windowOpened(final WindowEvent event) {
				resynchronizeMaximizedFrame(frame, frame.getExtendedState());
			}

			@Override
			public void windowStateChanged(final WindowEvent event) {
				if ((event.getOldState() & Frame.MAXIMIZED_BOTH) != Frame.MAXIMIZED_BOTH
						&& (event.getNewState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
					resynchronizeMaximizedFrame(frame, event.getNewState());
				}
			}
		};
		frame.addWindowListener(frameResynchronizer);
		frame.addWindowStateListener(frameResynchronizer);
	}


	public void openMapsOnStart() {
	    mapViewWindows.loadLayout();
    }

	public void focusTo(MapView currentMapView, Runnable onFocus) {
	    mapViewWindows.focusMapViewLater(currentMapView, onFocus);
    }

	@Override
	public void previousMapView() {
		mapViewWindows.selectPreviousMapView();

	}

	@Override
	public void nextMapView() {
		mapViewWindows.selectNextMapView();
	}

	@Override
	public void setFullScreen(final JFrame frame, boolean fullScreen) {
		super.setFullScreen(frame, fullScreen);
		mapViewWindows.setTabAreaVisiblePolicy(frame, ! fullScreen);
	}

	@Override
	public Component getCurrentRootComponent() {
		final Component mapViewComponent = selectedMapView();
		if (mapViewComponent == null) {
			return mainFrame;
		}
		final Component rootComponent = SwingUtilities.getRoot(mapViewComponent);
		if (rootComponent != null)
			return rootComponent;
		else
			return mainFrame;
	}


	@Override
	public JFrame getMainFrameComponent() {
		return mainFrame;
	}

	@Override
	public JFrame getMenuComponent() {
		return mainFrame.getMenuBar() != null ? mainFrame : frameComponentMover.getUIFrame();
	}

	@Override
	public List<? extends Component> getMapViewVector() {
		return mapViewWindows != null ? mapViewWindows.getMapViewVector() : null;
	}

	@Override
	public void openMapNextView() {
		mapViewWindows.selectMapNextView(selectedMapView());
	}

	@Override
	public void openMapPreviousView() {
		mapViewWindows.selectMapPreviousView(selectedMapView());
	}

	private  JComponent selectedMapView() {
		return controller.getMapViewManager().getMapViewComponent();
	}
}
