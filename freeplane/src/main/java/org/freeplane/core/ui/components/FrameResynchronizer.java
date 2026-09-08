package org.freeplane.core.ui.components;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

/**
 * Repairs the stale AWT window coordinate cache on the X11 toolkit so that
 * heavyweight FlatLaf popup windows (menus, combo box dropdowns) open at the
 * correct screen position after the window manager changes the frame geometry
 * (maximize, unmaximize, or arbitrary move). OpenJDK 11 with KWin/X11 keeps
 * reporting the pre-change origin via {@link java.awt.Component#getLocationOnScreen()}
 * long after the native window moved, which misplaces popups and makes them
 * dismiss on release.
 *
 * <p>Install once per top-level {@link JFrame} that hosts popup menus; the
 * resynchronizer acts only when the toolkit is X11 and the cached coordinates
 * actually differ from the native geometry.</p>
 */
public final class FrameResynchronizer extends WindowAdapter implements ComponentListener {
	private static final String X11_TOOLKIT_CLASS_NAME = "sun.awt.X11.XToolkit";
	private static final int RESYNCHRONIZATION_TOLERANCE = 4;

	/**
	 * Installs a {@link FrameResynchronizer} on the given frame. The listener
	 * must be added before the frame is shown so that startup-maximized windows
	 * are covered by {@code windowOpened} as well.
	 */
	public static void install(final JFrame frame) {
		final FrameResynchronizer frameResynchronizer = new FrameResynchronizer(frame);
		frame.addWindowListener(frameResynchronizer);
		frame.addWindowStateListener(frameResynchronizer);
		frame.addComponentListener(frameResynchronizer);
	}

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

	private static boolean isNormalWindowState(final int windowState) {
		return (windowState & (Frame.MAXIMIZED_BOTH | Frame.ICONIFIED)) == 0;
	}
}
