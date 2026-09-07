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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
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
	private static final int RESYNCHRONIZATION_TOLERANCE = 4;

	static boolean needsFrameResynchronization(final boolean isX11Toolkit, final int windowState,
			final Rectangle frameBounds, final Rectangle targetBounds) {
		return isX11Toolkit && (windowState & Frame.ICONIFIED) == 0
				&& frameBounds != null && targetBounds != null
				&& (frameBounds.x != targetBounds.x || frameBounds.y != targetBounds.y);
	}

	static boolean needsNormalWindowResynchronization(final boolean isX11Toolkit, final int windowState,
			final Rectangle frameBounds, final Rectangle nativeBounds) {
		if (!isX11Toolkit
				|| (windowState & (Frame.MAXIMIZED_BOTH | Frame.ICONIFIED)) != 0
				|| frameBounds == null || nativeBounds == null) {
			return false;
		}
		// Only trust the native geometry while the window sizes agree. A size
		// difference indicates the window manager is still placing or resizing the
		// window, and acting on such transient geometry makes the window shrink.
		if (Math.abs(frameBounds.width - nativeBounds.width) > RESYNCHRONIZATION_TOLERANCE
				|| Math.abs(frameBounds.height - nativeBounds.height) > RESYNCHRONIZATION_TOLERANCE) {
			return false;
		}
		return Math.abs(frameBounds.x - nativeBounds.x) > RESYNCHRONIZATION_TOLERANCE
				|| Math.abs(frameBounds.y - nativeBounds.y) > RESYNCHRONIZATION_TOLERANCE;
	}

	static boolean needsMaximizedFrameResynchronization(final boolean isX11Toolkit, final int windowState,
			final Rectangle frameBounds, final Rectangle screenBounds) {
		return (windowState & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH
				&& needsFrameResynchronization(isX11Toolkit, windowState, frameBounds, screenBounds);
	}

	static boolean needsNormalFrameResynchronization(final boolean isX11Toolkit, final int oldState,
			final int newState, final Rectangle frameBounds, final Rectangle normalBounds) {
		return (oldState & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH
				&& (newState & Frame.MAXIMIZED_BOTH) != Frame.MAXIMIZED_BOTH
				&& needsFrameResynchronization(isX11Toolkit, newState, frameBounds, normalBounds);
	}

	static Rectangle findTargetScreenBounds(final Rectangle frameBounds, final Rectangle[] screenBounds) {
		if (frameBounds == null || screenBounds == null) {
			return null;
		}
		Rectangle targetBounds = null;
		long largestIntersectionArea = 0;
		for (final Rectangle screenBound : screenBounds) {
			if (screenBound == null) {
				continue;
			}
			final Rectangle intersection = frameBounds.intersection(screenBound);
			final long intersectionArea = (long) Math.max(0, intersection.width)
					* Math.max(0, intersection.height);
			if (intersectionArea > largestIntersectionArea) {
				largestIntersectionArea = intersectionArea;
				targetBounds = screenBound;
			}
		}
		return targetBounds == null ? null : new Rectangle(targetBounds);
	}

	private static boolean isX11Toolkit() {
		return X11_TOOLKIT_CLASS_NAME.equals(Toolkit.getDefaultToolkit().getClass().getName());
	}

	private static boolean resynchronizeMaximizedFrame(final JFrame frame, final int windowState,
			final Rectangle nativeBounds, final Rectangle normalBounds) {
		Rectangle targetBounds = null;
		if (nativeBounds != null) {
			targetBounds = findTargetScreenBounds(nativeBounds, getScreenBounds());
		}
		if (targetBounds == null) {
			targetBounds = findTargetScreenBounds(normalBounds, getScreenBounds());
		}
		if (targetBounds != null) {
			if (needsMaximizedFrameResynchronization(isX11Toolkit(), windowState, frame.getBounds(), targetBounds)) {
				resynchronizeFrame(frame, windowState, targetBounds);
				return false;
			}
			return true;
		}
		final GraphicsConfiguration graphicsConfiguration = frame.getGraphicsConfiguration();
		if (graphicsConfiguration == null) {
			return true;
		}
		if (needsMaximizedFrameResynchronization(isX11Toolkit(), windowState, frame.getBounds(), graphicsConfiguration.getBounds())) {
			resynchronizeFrame(frame, windowState, graphicsConfiguration.getBounds());
			return false;
		}
		return true;
	}

	private static void resynchronizeNormalFrame(final JFrame frame, final int windowState,
			final Rectangle nativeBounds) {
		if (needsNormalWindowResynchronization(isX11Toolkit(), windowState, frame.getBounds(), nativeBounds)) {
			frame.setBounds(nativeBounds);
			frame.validate();
		}
	}

	/**
	 * Returns the current window manager frame bounds (including decorations) of the
	 * given component as a screen rectangle in logical pixels, or {@code null} if the
	 * bounds cannot be queried or are implausible (e.g. while the window is still
	 * being placed). Only available on the X11 toolkit and only when the component
	 * has a native shell window.
	 */
	static Rectangle getNativeWindowBounds(final Component component) {
		if (!isX11Toolkit() || component == null) {
			return null;
		}
		com.sun.jna.platform.unix.X11.Display display = null;
		try {
			final long shell = java.security.AccessController.doPrivileged(
					new java.security.PrivilegedExceptionAction<Long>() {
						@Override
						public Long run() throws Exception {
							final Class<?> awtAccessor = Class.forName("sun.awt.AWTAccessor");
							final Object componentAccessor = awtAccessor.getMethod("getComponentAccessor").invoke(null);
							final java.lang.reflect.Method getPeer = Class.forName("sun.awt.AWTAccessor$ComponentAccessor")
									.getMethod("getPeer", Component.class);
							getPeer.setAccessible(true);
							final Object peer = getPeer.invoke(componentAccessor, component);
							if (peer == null) {
								return 0L;
							}
							final java.lang.reflect.Method getShell = peer.getClass().getMethod("getShell");
							getShell.setAccessible(true);
							return ((Number) getShell.invoke(peer)).longValue();
						}
					});
			if (shell == 0) {
				return null;
			}
			display = com.sun.jna.platform.unix.X11.INSTANCE.XOpenDisplay(null);
			if (display == null) {
				return null;
			}
			final com.sun.jna.platform.unix.X11.Window root = com.sun.jna.platform.unix.X11.INSTANCE.XDefaultRootWindow(display);
			final com.sun.jna.platform.unix.X11.Window shellWindow = new com.sun.jna.platform.unix.X11.Window(shell);
			// Query the client window's root position and size.
			final com.sun.jna.ptr.IntByReference x = new com.sun.jna.ptr.IntByReference();
			final com.sun.jna.ptr.IntByReference y = new com.sun.jna.ptr.IntByReference();
			final com.sun.jna.platform.unix.X11.WindowByReference child = new com.sun.jna.platform.unix.X11.WindowByReference();
			final boolean translated = com.sun.jna.platform.unix.X11.INSTANCE.XTranslateCoordinates(
					display, shellWindow, root, 0, 0, x, y, child);
			if (!translated) {
				return null;
			}
			final com.sun.jna.ptr.IntByReference rootX = new com.sun.jna.ptr.IntByReference();
			final com.sun.jna.ptr.IntByReference rootY = new com.sun.jna.ptr.IntByReference();
			final com.sun.jna.ptr.IntByReference width = new com.sun.jna.ptr.IntByReference();
			final com.sun.jna.ptr.IntByReference height = new com.sun.jna.ptr.IntByReference();
			final com.sun.jna.ptr.IntByReference border = new com.sun.jna.ptr.IntByReference();
			final com.sun.jna.ptr.IntByReference depth = new com.sun.jna.ptr.IntByReference();
			final com.sun.jna.platform.unix.X11.WindowByReference geometryRoot = new com.sun.jna.platform.unix.X11.WindowByReference();
			final int status = com.sun.jna.platform.unix.X11.INSTANCE.XGetGeometry(
					display, new com.sun.jna.platform.unix.X11.Drawable(shell), geometryRoot,
					rootX, rootY, width, height, border, depth);
			if (status == 0) {
				return null;
			}
			// Add the window manager decoration extents to obtain the outer frame
			// bounds as java.awt.Component bounds are reported. EWMH order is
			// left, right, top, bottom.
			final int[] frameExtents = readFrameExtents(display, shellWindow);
			final int left = frameExtents[0];
			final int right = frameExtents[1];
			final int top = frameExtents[2];
			final int bottom = frameExtents[3];
			final int outerWidth = width.getValue() + left + right;
			final int outerHeight = height.getValue() + top + bottom;
			if (outerWidth < 100 || outerHeight < 100) {
				// Implausible geometry; the window is probably not placed yet.
				return null;
			}
			final GraphicsConfiguration gc = component.getGraphicsConfiguration();
			final java.awt.geom.AffineTransform transform = gc == null ? null : gc.getDefaultTransform();
			final double scaleX = transform == null ? 1.0 : transform.getScaleX();
			final double scaleY = transform == null ? 1.0 : transform.getScaleY();
			final int logicalX = (int) Math.round((x.getValue() - left) / (scaleX > 0 ? scaleX : 1.0));
			final int logicalY = (int) Math.round((y.getValue() - top) / (scaleY > 0 ? scaleY : 1.0));
			final int logicalW = (int) Math.round(outerWidth / (scaleX > 0 ? scaleX : 1.0));
			final int logicalH = (int) Math.round(outerHeight / (scaleY > 0 ? scaleY : 1.0));
			return new Rectangle(logicalX, logicalY, logicalW, logicalH);
		}
		catch (Throwable throwable) {
			return null;
		}
		finally {
			if (display != null) {
				try {
					com.sun.jna.platform.unix.X11.INSTANCE.XCloseDisplay(display);
				}
				catch (Throwable ignored) {
				}
			}
		}
	}

	/**
	 * Reads the {@code _NET_FRAME_EXTENTS} property of the given window as an array of
	 * left, top, right, bottom decoration sizes. Returns an array of zeros when the
	 * property is missing or unreadable.
	 */
	private static int[] readFrameExtents(final com.sun.jna.platform.unix.X11.Display display,
			final com.sun.jna.platform.unix.X11.Window window) {
		final int[] result = new int[4];
		try {
			final com.sun.jna.platform.unix.X11.Atom atom = com.sun.jna.platform.unix.X11.INSTANCE.XInternAtom(display,
					"_NET_FRAME_EXTENTS", false);
			final com.sun.jna.platform.unix.X11.AtomByReference actualType = new com.sun.jna.platform.unix.X11.AtomByReference();
			final com.sun.jna.ptr.IntByReference actualFormat = new com.sun.jna.ptr.IntByReference();
			final com.sun.jna.ptr.NativeLongByReference nitems = new com.sun.jna.ptr.NativeLongByReference();
			final com.sun.jna.ptr.NativeLongByReference bytesAfter = new com.sun.jna.ptr.NativeLongByReference();
			final com.sun.jna.ptr.PointerByReference property = new com.sun.jna.ptr.PointerByReference();
			final int status = com.sun.jna.platform.unix.X11.INSTANCE.XGetWindowProperty(display, window, atom,
					new com.sun.jna.NativeLong(0), new com.sun.jna.NativeLong(4), false, new com.sun.jna.platform.unix.X11.Atom(0),
					actualType, actualFormat, nitems, bytesAfter, property);
			if (status == 0 && actualFormat.getValue() == 32 && property.getValue() != null) {
				final int count = (int) Math.min(4, nitems.getValue().longValue());
				if (count >= 4) {
					// On LP64 systems Xlib returns 32-bit format properties as 64-bit
					// longs; the EWMH order is left, right, top, bottom.
					final long[] values = property.getValue().getLongArray(0, 4);
					for (int i = 0; i < result.length; i++) {
						result[i] = (int) values[i];
					}
				}
			}
			if (property.getValue() != null) {
				com.sun.jna.platform.unix.X11.INSTANCE.XFree(property.getValue());
			}
		}
		catch (Throwable ignored) {
		}
		return result;
	}

	private static Rectangle[] getScreenBounds() {
		final GraphicsDevice[] screenDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		final Rectangle[] screenBounds = new Rectangle[screenDevices.length];
		for (int i = 0; i < screenDevices.length; i++) {
			final GraphicsConfiguration graphicsConfiguration = screenDevices[i].getDefaultConfiguration();
			screenBounds[i] = graphicsConfiguration == null ? null : graphicsConfiguration.getBounds();
		}
		return screenBounds;
	}

	private static void resynchronizeFrame(final JFrame frame, final int windowState,
			final Rectangle targetBounds) {
		if (needsFrameResynchronization(isX11Toolkit(), windowState, frame.getBounds(), targetBounds)) {
			frame.setBounds(targetBounds);
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
        frame.setBounds(getStoredFrameBounds(frame));
		installMaximizedFrameResynchronization(frame);
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

	private static void installMaximizedFrameResynchronization(final JFrame frame) {
		final FrameResynchronizer frameResynchronizer = new FrameResynchronizer(frame);
		frame.addWindowListener(frameResynchronizer);
		frame.addWindowStateListener(frameResynchronizer);
		frame.addComponentListener(frameResynchronizer);
	}

	private static final class FrameResynchronizer extends WindowAdapter implements ComponentListener {
		private final JFrame frame;
		private Rectangle normalBounds;
		private int lastKnownWindowState;
		private boolean isResynchronizing;
		private javax.swing.Timer deferredResynchronizationTimer;
		private int deferredResynchronizationAttempts;
		private static final int MAX_DEFERRED_RESYNCHRONIZATION_ATTEMPTS = 10;

		private FrameResynchronizer(final JFrame frame) {
			this.frame = frame;
			normalBounds = new Rectangle(frame.getBounds());
			lastKnownWindowState = frame.getExtendedState();
		}

		@Override
		public void windowOpened(final WindowEvent event) {
			lastKnownWindowState = frame.getExtendedState();
			resynchronizeOnStableState();
			scheduleDeferredResynchronization();
		}

		@Override
		public void windowStateChanged(final WindowEvent event) {
			final int oldState = event.getOldState();
			final int newState = event.getNewState();
			lastKnownWindowState = newState;
			if ((oldState & Frame.MAXIMIZED_BOTH) != Frame.MAXIMIZED_BOTH
					&& (newState & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
				resynchronizeOnStableState();
			}
			else if (needsNormalFrameResynchronization(isX11Toolkit(), oldState, newState,
					frame.getBounds(), normalBounds)) {
				runGuardedResynchronization(new Runnable() {
					@Override
					public void run() {
						resynchronizeFrame(frame, newState, normalBounds);
						rememberNormalBounds();
					}
				});
			}
		}

		@Override
		public void componentMoved(final ComponentEvent event) {
			handleComponentGeometryChanged();
		}

		@Override
		public void componentResized(final ComponentEvent event) {
			handleComponentGeometryChanged();
		}

		@Override
		public void componentShown(final ComponentEvent event) {
		}

		@Override
		public void componentHidden(final ComponentEvent event) {
		}

		private void handleComponentGeometryChanged() {
			if (isResynchronizing) {
				return;
			}
			final int windowState = frame.getExtendedState();
			if ((windowState & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
				resynchronizeOnStableState();
			}
			else if (isNormalWindowState(windowState)) {
				resynchronizeOnStableState();
			}
			else {
				rememberNormalBounds();
			}
			scheduleDeferredResynchronization();
		}

		private void scheduleDeferredResynchronization() {
			// The window manager may still be moving the window when the component
			// event is delivered; the AWT position cache is not necessarily updated
			// by the same event. Recheck shortly afterwards until the geometry is
			// stable, but do not retry forever.
			if (deferredResynchronizationTimer != null) {
				deferredResynchronizationTimer.stop();
			}
			deferredResynchronizationAttempts = 0;
			startDeferredResynchronization(200);
		}

		private void startDeferredResynchronization(final int delay) {
			deferredResynchronizationTimer = new javax.swing.Timer(delay, new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent event) {
					deferredResynchronizationTimer = null;
					final boolean stable = resynchronizeOnStableState();
					if (!stable && deferredResynchronizationAttempts < MAX_DEFERRED_RESYNCHRONIZATION_ATTEMPTS) {
						deferredResynchronizationAttempts++;
						startDeferredResynchronization(200);
					}
				}
			});
			deferredResynchronizationTimer.setRepeats(false);
			deferredResynchronizationTimer.start();
		}

		private boolean resynchronizeOnStableState() {
			final boolean[] stable = new boolean[1];
			runGuardedResynchronization(new Runnable() {
				@Override
				public void run() {
					final int windowState = frame.getExtendedState();
					lastKnownWindowState = windowState;
					final Rectangle nativeBounds = getNativeWindowBounds(frame);
					if ((windowState & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
						stable[0] = resynchronizeMaximizedFrame(frame, windowState, nativeBounds, normalBounds);
					}
					else if (isNormalWindowState(windowState)) {
						// Native geometry that differs in size is likely transient; keep
						// retrying until the window manager has finished placing the window.
						if (nativeBounds == null) {
							stable[0] = true;
						}
						else {
							final Rectangle frameBounds = frame.getBounds();
							final boolean sizesMatch =
									Math.abs(frameBounds.width - nativeBounds.width) <= RESYNCHRONIZATION_TOLERANCE
											&& Math.abs(frameBounds.height - nativeBounds.height) <= RESYNCHRONIZATION_TOLERANCE;
							stable[0] = sizesMatch && !needsNormalWindowResynchronization(isX11Toolkit(),
									windowState, frameBounds, nativeBounds);
						}
						if (nativeBounds != null) {
							resynchronizeNormalFrame(frame, windowState, nativeBounds);
						}
						rememberNormalBounds();
					}
					else {
						stable[0] = true;
					}
				}
			});
			return stable[0];
		}

		private void runGuardedResynchronization(final Runnable resynchronization) {
			if (isResynchronizing) {
				return;
			}
			isResynchronizing = true;
			try {
				resynchronization.run();
			}
			finally {
				isResynchronizing = false;
			}
		}

		private void rememberNormalBounds() {
			if (isNormalWindowState(lastKnownWindowState) && isNormalWindowState(frame.getExtendedState())) {
				normalBounds = new Rectangle(frame.getBounds());
			}
		}
	}

	private static boolean isNormalWindowState(final int windowState) {
		return (windowState & (Frame.MAXIMIZED_BOTH | Frame.ICONIFIED)) == 0;
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
