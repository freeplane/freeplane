package org.freeplane.core.ui.components;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JFrame;

/**
 * Computes the screen translation that reconciles the stale AWT component
 * coordinate cache with the native X11 window geometry.
 *
 * <p>The window manager on X11 can move a window, while OpenJDK keeps reporting
 * the pre-move origin from {@link Component#getLocationOnScreen()}. AWT component
 * bounds are therefore not a reliable source of truth for popup placement. The
 * native window manager frame origin is, as long as it is combined with the
 * owner's position expressed in the frame's own coordinate system: the decoration
 * offset cancels because the native query returns the outer frame origin.</p>
 *
 * <pre>
 * expectedOwnerOnScreen = nativeFrameOrigin + ownerLocationWithinFrame
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
		 * Returns the native window manager frame bounds of the given frame in the
		 * same logical screen coordinate space used by AWT, or {@code null} when the
		 * geometry is unavailable or implausible.
		 */
		Rectangle getNativeFrameBounds(JFrame frame);

		/**
		 * Returns the owner origin in the frame's own coordinate system, or
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
	 * {@code null} when the calibration is unavailable. Every unavailable or
	 * implausible geometry value, and every runtime failure while reading it, is
	 * reported as {@code null} so that callers keep the original popup coordinates.
	 */
	static Point calculateCorrection(final Component owner, final JFrame frame,
			final GeometryProvider geometryProvider) {
		if (owner == null || frame == null || geometryProvider == null) {
			return null;
		}
		try {
			final Rectangle nativeFrameBounds = geometryProvider.getNativeFrameBounds(frame);
			final Point ownerLocationWithinFrame = geometryProvider.getOwnerLocationWithinFrame(owner, frame);
			final Point ownerLocationOnScreen = geometryProvider.getOwnerLocationOnScreen(owner);
			if (nativeFrameBounds == null || nativeFrameBounds.width <= 0 || nativeFrameBounds.height <= 0
					|| ownerLocationWithinFrame == null || ownerLocationOnScreen == null) {
				return null;
			}
			final int expectedX = nativeFrameBounds.x + ownerLocationWithinFrame.x;
			final int expectedY = nativeFrameBounds.y + ownerLocationWithinFrame.y;
			return new Point(expectedX - ownerLocationOnScreen.x, expectedY - ownerLocationOnScreen.y);
		}
		catch (RuntimeException unavailableGeometry) {
			return null;
		}
	}
}
