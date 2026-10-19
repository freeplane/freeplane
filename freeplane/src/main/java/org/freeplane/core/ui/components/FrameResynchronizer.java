package org.freeplane.core.ui.components;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;

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
	private static final Set<JFrame> REGISTERED_FRAMES = Collections.newSetFromMap(new WeakHashMap<JFrame, Boolean>());

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
		REGISTERED_FRAMES.add(frame);
		refreshPopupFactory();
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

	/**
	 * Decides whether a maximized frame still needs a bounds request.
	 *
	 * <p>The decision is based on the geometry the window manager reports, not on
	 * the AWT bounds. On X11 the window manager maximizes the <em>client</em> area to
	 * the work area while AWT keeps reporting the decorated <em>outer</em> frame
	 * (for example AWT {@code -4,-34 1928x1120} for a client {@code 0,0 1920x1080}).
	 * Comparing AWT bounds against the work area therefore never matches on a
	 * correctly maximized window, and the frame is re-requested over and over for
	 * geometry the window manager already produced.</p>
	 *
	 * @param nativeBounds the geometry reported by the window manager
	 * @param targetBounds the work area the maximized frame should cover
	 */
	static boolean needsMaximizedFrameResynchronization(final boolean isX11Toolkit, final int windowState,
			final Rectangle nativeBounds, final Rectangle targetBounds) {
		return (windowState & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH
				&& isX11Toolkit && (windowState & Frame.ICONIFIED) == 0
				&& nativeBounds != null && targetBounds != null
				&& !nativeBounds.equals(targetBounds);
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

	/**
	 * Returns the usable work area of a screen, derived by shrinking the screen
	 * rectangle by the given screen insets. Zero insets mean the complete screen
	 * rectangle is usable. Returns {@code null} when the inputs are missing or the
	 * derived rectangle has no area.
	 */
	static Rectangle getWorkAreaBounds(final Rectangle screenBounds, final Insets insets) {
		if (screenBounds == null || insets == null) {
			return null;
		}
		final int width = screenBounds.width - insets.left - insets.right;
		final int height = screenBounds.height - insets.top - insets.bottom;
		if (width <= 0 || height <= 0) {
			return null;
		}
		return new Rectangle(screenBounds.x + insets.left, screenBounds.y + insets.top, width, height);
	}

	/**
	 * Resolves the work area a maximized frame belongs to: the work area matching the
	 * native window geometry first, the one matching the remembered normal bounds
	 * second, and a copy of the given fallback work area last. Returns {@code null}
	 * when no work area can be determined, so that callers keep the frame stable
	 * instead of falling back to the full screen.
	 */
	static Rectangle findTargetMaximizedBounds(final Rectangle nativeBounds, final Rectangle normalBounds,
			final Rectangle[] workAreaBounds, final Rectangle fallbackWorkArea) {
		Rectangle targetBounds = findTargetScreenBounds(nativeBounds, workAreaBounds);
		if (targetBounds == null) {
			targetBounds = findTargetScreenBounds(normalBounds, workAreaBounds);
		}
		if (targetBounds != null) {
			return targetBounds;
		}
		return fallbackWorkArea == null ? null : new Rectangle(fallbackWorkArea);
	}

	private static boolean isX11Toolkit() {
		return X11_TOOLKIT_CLASS_NAME.equals(Toolkit.getDefaultToolkit().getClass().getName());
	}

	private static final PopupCoordinateCalibrator.GeometryProvider DEFAULT_POPUP_GEOMETRY_PROVIDER =
			new PopupCoordinateCalibrator.GeometryProvider() {
				@Override
				public Point getNativeClientOrigin(final JFrame frame) {
					// The popup correction is anchored to the native client origin and to
					// the owner position measured from the frame's root pane. Both are
					// expressed relative to the client area, so decoration extents appear
					// on neither side and cancel. Do not substitute the outer frame bounds
					// here: pairing an outer origin with a root-pane-relative position
					// reintroduces the decoration offset as a constant popup error.
					return FrameResynchronizer.getNativeClientOrigin(frame);
				}

				@Override
				public Point getOwnerLocationWithinFrame(final Component owner, final JFrame frame) {
					final JRootPane rootPane = frame.getRootPane();
					if (owner == null || rootPane == null) {
						return null;
					}
					return SwingUtilities.convertPoint(owner, 0, 0, rootPane);
				}

				@Override
				public Point getOwnerLocationOnScreen(final Component owner) {
					try {
						if (owner == null || !owner.isShowing()) {
							return null;
						}
						return owner.getLocationOnScreen();
					}
					catch (RuntimeException unavailableLocation) {
						return null;
					}
				}
			};

	public static void refreshPopupFactory() {
		final PopupFactory currentFactory = PopupFactory.getSharedInstance();
		final PopupFactory refreshedFactory = refreshPopupFactory(isX11Toolkit(), currentFactory, REGISTERED_FRAMES,
				DEFAULT_POPUP_GEOMETRY_PROVIDER);
		if (refreshedFactory != currentFactory) {
			PopupFactory.setSharedInstance(refreshedFactory);
		}
	}

	static PopupFactory refreshPopupFactory(final boolean isX11Toolkit, final PopupFactory currentFactory,
			final Set<JFrame> registeredFrames,
			final PopupCoordinateCalibrator.GeometryProvider geometryProvider) {
		return ensurePopupFactory(isX11Toolkit, currentFactory, registeredFrames, geometryProvider);
	}

	static PopupFactory ensurePopupFactory(final boolean isX11Toolkit, final PopupFactory currentFactory,
			final Set<JFrame> registeredFrames,
			final PopupCoordinateCalibrator.GeometryProvider geometryProvider) {
		if (!isX11Toolkit || currentFactory == null || registeredFrames == null || registeredFrames.isEmpty()
				|| currentFactory instanceof X11PopupFactory) {
			return currentFactory;
		}
		final X11PopupFactory.PopupOwnerResolver ownerResolver = new X11PopupFactory.PopupOwnerResolver() {
			@Override
			public JFrame findFrame(final Component owner) {
				if (owner == null) {
					return null;
				}
				if (owner instanceof Window) {
					return registeredFrames.contains(owner) ? (JFrame) owner : null;
				}
				final Window ancestor = SwingUtilities.getWindowAncestor(owner);
				return ancestor instanceof JFrame && registeredFrames.contains(ancestor) ? (JFrame) ancestor : null;
			}
		};
		return new X11PopupFactory(currentFactory, ownerResolver, geometryProvider);
	}

	static final class MaximizedResynchronizationGuard {
		private static final int MAX_ATTEMPTS = 10;
		private Rectangle targetBounds;
		private int attempts;

		boolean shouldRequest(final Rectangle currentBounds, final Rectangle targetBounds) {
			if (currentBounds == null || targetBounds == null || currentBounds.equals(targetBounds)) {
				reset();
				return false;
			}
			if (!targetBounds.equals(this.targetBounds)) {
				this.targetBounds = new Rectangle(targetBounds);
				attempts = 0;
			}
			if (attempts >= MAX_ATTEMPTS) {
				return false;
			}
			attempts++;
			return true;
		}

		boolean isExhausted() {
			return attempts >= MAX_ATTEMPTS;
		}

		void reset() {
			targetBounds = null;
			attempts = 0;
		}
	}

	static boolean shouldScheduleDeferredResynchronization(final boolean maximized, final boolean stable) {
		return !maximized || !stable;
	}

	interface FrameGeometry {
		boolean isX11Toolkit();

		Rectangle getNativeWindowBounds(Component component);

		Rectangle[] getScreenWorkAreas();

		Insets getScreenInsets(GraphicsConfiguration graphicsConfiguration);
	}

	private static final FrameGeometry DEFAULT_FRAME_GEOMETRY = new FrameGeometry() {
		@Override
		public boolean isX11Toolkit() {
			return FrameResynchronizer.isX11Toolkit();
		}

		@Override
		public Rectangle getNativeWindowBounds(final Component component) {
			return FrameResynchronizer.getNativeWindowBounds(component);
		}

		@Override
		public Rectangle[] getScreenWorkAreas() {
			return FrameResynchronizer.getScreenWorkAreas();
		}

		@Override
		public Insets getScreenInsets(final GraphicsConfiguration graphicsConfiguration) {
			return FrameResynchronizer.getScreenInsets(graphicsConfiguration);
		}
	};

	private boolean resynchronizeMaximizedFrame(final JFrame frame, final int windowState,
			final Rectangle nativeBounds, final Rectangle normalBounds) {
		final Rectangle[] workAreaBounds = frameGeometry.getScreenWorkAreas();
		final GraphicsConfiguration graphicsConfiguration = frame.getGraphicsConfiguration();
		final Rectangle fallbackWorkArea = graphicsConfiguration == null ? null
				: getScreenWorkAreaBounds(graphicsConfiguration, frameGeometry);
		final Rectangle targetBounds = findTargetMaximizedBounds(nativeBounds, normalBounds, workAreaBounds,
				fallbackWorkArea);
		if (targetBounds == null) {
			return true;
		}
		final Rectangle currentBounds = frame.getBounds();
		if (currentBounds == null || currentBounds.equals(targetBounds)) {
			maximizedResynchronizationGuard.reset();
			return true;
		}
		// Ask the window manager what the window actually covers. AWT bounds are
		// the decorated outer frame and never equal the client work area, so they
		// cannot tell a correctly maximized window from a displaced one.
		if (!needsMaximizedFrameResynchronization(frameGeometry.isX11Toolkit(), windowState, nativeBounds,
				targetBounds)
				|| !maximizedResynchronizationGuard.shouldRequest(currentBounds, targetBounds)) {
			return true;
		}
		frame.setBounds(targetBounds);
		frame.validate();
		return false;
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
	static long getShellWindowId(final Component component) {
		try {
			return java.security.AccessController.doPrivileged(
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
		}
		catch (Throwable unavailableShell) {
			return 0L;
		}
	}

	/**
	 * Returns the origin of the native client area of the given component's shell
	 * window in logical screen coordinates, or {@code null} when it cannot be
	 * queried. This origin belongs to the same coordinate space as
	 * {@code java.awt.Component#getLocationOnScreen()}, so a position expressed
	 * relative to the root pane can be added to it directly.
	 */
	static Point getNativeClientOrigin(final Component component) {
		if (!isX11Toolkit() || component == null) {
			return null;
		}
		com.sun.jna.platform.unix.X11.Display display = null;
		try {
			final long shell = getShellWindowId(component);
			if (shell == 0) {
				return null;
			}
			display = com.sun.jna.platform.unix.X11.INSTANCE.XOpenDisplay(null);
			if (display == null) {
				return null;
			}
			final com.sun.jna.platform.unix.X11.Window root = com.sun.jna.platform.unix.X11.INSTANCE.XDefaultRootWindow(display);
			final com.sun.jna.platform.unix.X11.Window shellWindow = new com.sun.jna.platform.unix.X11.Window(shell);
			final com.sun.jna.ptr.IntByReference x = new com.sun.jna.ptr.IntByReference();
			final com.sun.jna.ptr.IntByReference y = new com.sun.jna.ptr.IntByReference();
			final com.sun.jna.platform.unix.X11.WindowByReference child = new com.sun.jna.platform.unix.X11.WindowByReference();
			final boolean translated = com.sun.jna.platform.unix.X11.INSTANCE.XTranslateCoordinates(
					display, shellWindow, root, 0, 0, x, y, child);
			if (!translated) {
				return null;
			}
			final GraphicsConfiguration gc = component.getGraphicsConfiguration();
			final java.awt.geom.AffineTransform transform = gc == null ? null : gc.getDefaultTransform();
			final double scaleX = transform == null ? 1.0 : transform.getScaleX();
			final double scaleY = transform == null ? 1.0 : transform.getScaleY();
			return new Point((int) Math.round(x.getValue() / (scaleX > 0 ? scaleX : 1.0)),
					(int) Math.round(y.getValue() / (scaleY > 0 ? scaleY : 1.0)));
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

	static Rectangle getNativeWindowBounds(final Component component) {
		if (!isX11Toolkit() || component == null) {
			return null;
		}
		com.sun.jna.platform.unix.X11.Display display = null;
		try {
			final long shell = getShellWindowId(component);
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

	private static Rectangle[] getScreenWorkAreas() {
		final GraphicsDevice[] screenDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		final GraphicsConfiguration[] graphicsConfigurations = new GraphicsConfiguration[screenDevices.length];
		for (int i = 0; i < screenDevices.length; i++) {
			graphicsConfigurations[i] = screenDevices[i].getDefaultConfiguration();
		}
		return getScreenWorkAreas(graphicsConfigurations, DEFAULT_FRAME_GEOMETRY);
	}

	static Rectangle[] getScreenWorkAreas(final GraphicsConfiguration[] graphicsConfigurations,
			final FrameGeometry frameGeometry) {
		final Rectangle[] workAreaBounds = new Rectangle[graphicsConfigurations.length];
		for (int i = 0; i < graphicsConfigurations.length; i++) {
			final GraphicsConfiguration graphicsConfiguration = graphicsConfigurations[i];
			workAreaBounds[i] = graphicsConfiguration == null ? null
					: getScreenWorkAreaBounds(graphicsConfiguration, frameGeometry);
		}
		return workAreaBounds;
	}

	private static Rectangle getScreenWorkAreaBounds(final GraphicsConfiguration graphicsConfiguration,
			final FrameGeometry frameGeometry) {
		final Rectangle screenBounds = graphicsConfiguration.getBounds();
		final Insets screenInsets;
		try {
			screenInsets = frameGeometry.getScreenInsets(graphicsConfiguration);
		}
		catch (RuntimeException runtimeException) {
			return null;
		}
		return getWorkAreaBounds(screenBounds, screenInsets);
	}

	/**
	 * Returns the screen insets of the given configuration, or {@code null} when they
	 * cannot be determined. A missing result makes the screen's work area
	 * unavailable instead of silently treating the full screen as usable.
	 */
	private static Insets getScreenInsets(final GraphicsConfiguration graphicsConfiguration) {
		try {
			return Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration);
		}
		catch (RuntimeException runtimeException) {
			return null;
		}
	}

	private static void resynchronizeFrame(final JFrame frame, final int windowState,
			final Rectangle targetBounds) {
		if (needsFrameResynchronization(isX11Toolkit(), windowState, frame.getBounds(), targetBounds)) {
			frame.setBounds(targetBounds);
			frame.validate();
		}
	}

	private final JFrame frame;
	private final FrameGeometry frameGeometry;
	private Rectangle normalBounds;
	private final MaximizedResynchronizationGuard maximizedResynchronizationGuard =
			new MaximizedResynchronizationGuard();
	private int lastKnownWindowState;
	private boolean isResynchronizing;
	private javax.swing.Timer deferredResynchronizationTimer;
	private int deferredResynchronizationAttempts;
	private static final int MAX_DEFERRED_RESYNCHRONIZATION_ATTEMPTS = 10;

	private FrameResynchronizer(final JFrame frame) {
		this(frame, DEFAULT_FRAME_GEOMETRY);
	}

	FrameResynchronizer(final JFrame frame, final FrameGeometry frameGeometry) {
		this.frame = frame;
		this.frameGeometry = frameGeometry;
		normalBounds = new Rectangle(frame.getBounds());
		lastKnownWindowState = frame.getExtendedState();
	}

	@Override
	public void windowOpened(final WindowEvent event) {
		lastKnownWindowState = frame.getExtendedState();
		final boolean maximized = (lastKnownWindowState & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH;
		final boolean stable = resynchronizeOnStableState();
		if (shouldScheduleDeferredResynchronization(maximized, stable)) {
			scheduleDeferredResynchronization();
		}
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
		else if ((oldState & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH
				&& (newState & Frame.MAXIMIZED_BOTH) != Frame.MAXIMIZED_BOTH) {
			maximizedResynchronizationGuard.reset();
			if (needsNormalFrameResynchronization(isX11Toolkit(), oldState, newState,
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
		final boolean maximized = (windowState & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH;
		final boolean stable;
		if (maximized) {
			stable = resynchronizeOnStableState();
		}
		else if (isNormalWindowState(windowState)) {
			stable = resynchronizeOnStableState();
		}
		else {
			rememberNormalBounds();
			stable = true;
		}
		if (shouldScheduleDeferredResynchronization(maximized, stable)) {
			scheduleDeferredResynchronization();
		}
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
				final Rectangle nativeBounds = frameGeometry.getNativeWindowBounds(frame);
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
