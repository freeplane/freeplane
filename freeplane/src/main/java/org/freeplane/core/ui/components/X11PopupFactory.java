package org.freeplane.core.ui.components;

import java.awt.Component;
import java.awt.Point;

import javax.swing.JFrame;
import javax.swing.Popup;
import javax.swing.PopupFactory;

/**
 * Delegating {@link PopupFactory} adapter that translates the requested popup
 * coordinates into the native X11 screen space before handing the request to the
 * active factory.
 *
 * <p>The captured delegate remains the factory selected by the current look and
 * feel. This adapter does not reproduce, bypass, or subclass that implementation:
 * it only applies the owner-local translation from
 * {@link PopupCoordinateCalibrator} to the two integer coordinates and then calls
 * the delegate exactly once. Every other aspect of popup creation, including
 * heavyweight selection, tooltip adjustment, popup reuse, border handling, and
 * platform behavior, remains with the delegate.</p>
 *
 * <p>Swing stores the popup-type hint of {@code JPopupMenu.showPopup()} on the
 * object returned by {@code PopupFactory.getSharedInstance()}, which is this
 * adapter once installed. No code outside the {@code javax.swing} package can read
 * or forward that package-private hint, so this adapter intentionally carries no
 * popup-type state and performs no popup selection. Popup-class parity with the
 * unwrapped factory is verified by experiment instead.</p>
 */
final class X11PopupFactory extends PopupFactory {
	/**
	 * Finds the registered Freeplane frame that hosts the given popup owner, or
	 * returns {@code null} when the owner does not belong to a registered frame.
	 */
	interface PopupOwnerResolver {
		JFrame findFrame(Component owner);
	}

	private final PopupFactory delegate;
	private final PopupOwnerResolver ownerResolver;
	private final PopupCoordinateCalibrator.GeometryProvider geometryProvider;

	X11PopupFactory(final PopupFactory delegate, final PopupOwnerResolver ownerResolver,
			final PopupCoordinateCalibrator.GeometryProvider geometryProvider) {
		if (delegate == null) {
			throw new NullPointerException("delegate");
		}
		this.delegate = delegate;
		this.ownerResolver = ownerResolver;
		this.geometryProvider = geometryProvider;
	}

	/**
	 * Returns the captured active factory. Exposed for identity checks only; it
	 * neither exposes nor mutates any frame registry.
	 */
	PopupFactory getDelegate() {
		return delegate;
	}

	@Override
	public Popup getPopup(final Component owner, final Component contents, final int x, final int y) {
		int correctedX = x;
		int correctedY = y;
		final Point correction = calculateCorrection(owner);
		if (correction != null) {
			correctedX = x + correction.x;
			correctedY = y + correction.y;
		}
		// Delegate outside the calibration failure handler: calibration problems
		// only ever restore the original coordinates, while delegate exceptions
		// must propagate unchanged.
		return delegate.getPopup(owner, contents, correctedX, correctedY);
	}

	private Point calculateCorrection(final Component owner) {
		if (owner == null || ownerResolver == null || geometryProvider == null) {
			return null;
		}
		try {
			final JFrame frame = ownerResolver.findFrame(owner);
			if (frame == null) {
				return null;
			}
			return PopupCoordinateCalibrator.calculateCorrection(owner, frame, geometryProvider);
		}
		catch (RuntimeException unavailableOwner) {
			return null;
		}
	}
}
