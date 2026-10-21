package org.freeplane.core.ui.components;

import java.awt.Component;
import java.awt.Point;

import javax.swing.JFrame;

/**
 * Computes the screen translation that reconciles the stale AWT component
 * coordinate cache with the native X11 window geometry.
 *
 * <p>The window manager on X11 can move a window, while OpenJDK keeps reporting
 * the pre-move origin from {@link Component#getLocationOnScreen()}. AWT component
 * bounds are therefore not a reliable source of truth for popup placement. The
 * native client origin is, as long as it is combined with the owner's position
 * expressed relative to the frame's root pane. Both refer to the client area,
 * so decoration extents enter neither value.</p>
 *
 * <pre>
 * expectedOwnerOnScreen = nativeClientOrigin + ownerLocationWithinRootPane
 * correction            = expectedOwnerOnScreen - ownerLocationOnScreen
 * </pre>
 *
 * <p>The result is a plain per-request translation. This class never mutates the
 * owner or the frame, never requests new frame bounds, and never retries: it only
 * reports what should be added to the requested popup coordinates.</p>
 */
final class PopupCoordinateCalibrator {
	/**
	 * Supplies the three geometry values the correction is derived from. The
	 * production implementation queries native X11 geometry and AWT component
	 * locations; unit tests inject fixture values instead of opening a desktop.
	 */
	interface GeometryProvider {
		/**
		 * Returns the native client-area origin of the frame in logical screen
		 * coordinates. It must belong to the same coordinate space as
		 * {@link Component#getLocationOnScreen()}, and it must be paired with a
		 * position expressed relative to the frame's root pane so that decoration
		 * extents appear on neither side.
		 */
		Point getNativeClientOrigin(JFrame frame);

		/**
		 * Returns the owner origin relative to the frame's root pane, or
		 * {@code null} when the point cannot be converted.
		 */
		Point getOwnerLocationWithinFrame(Component owner, JFrame frame);

		/**
		 * Returns the AWT screen location of the owner, the location the popup request
		 * is based on, or {@code null} when the owner is not showing.
		 */
		Point getOwnerLocationOnScreen(Component owner);
	}

	private PopupCoordinateCalibrator() {
	}

	/**
	 * Returns the correction to add to the requested popup coordinates, or
	 * {@code null} when the calibration is unavailable. Every unavailable geometry
	 * value, and every runtime failure while reading it, is reported as
	 * {@code null} so that callers keep the original popup coordinates.
	 */
	static Point calculateCorrection(final Component owner, final JFrame frame,
			final GeometryProvider geometryProvider) {
		if (owner == null || frame == null || geometryProvider == null) {
			return null;
		}
		try {
			final Point nativeClientOrigin = geometryProvider.getNativeClientOrigin(frame);
			final Point ownerLocationWithinRootPane = geometryProvider.getOwnerLocationWithinFrame(owner, frame);
			final Point ownerLocationOnScreen = geometryProvider.getOwnerLocationOnScreen(owner);
			if (nativeClientOrigin == null || ownerLocationWithinRootPane == null || ownerLocationOnScreen == null) {
				return null;
			}
			final int expectedX = nativeClientOrigin.x + ownerLocationWithinRootPane.x;
			final int expectedY = nativeClientOrigin.y + ownerLocationWithinRootPane.y;
			return new Point(expectedX - ownerLocationOnScreen.x, expectedY - ownerLocationOnScreen.y);
		}
		catch (RuntimeException unavailableGeometry) {
			return null;
		}
	}
}
