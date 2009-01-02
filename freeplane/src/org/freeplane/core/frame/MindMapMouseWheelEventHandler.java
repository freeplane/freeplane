/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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

import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Iterator;
import java.util.Set;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.map.ModeController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.ui.IFreemindPropertyListener;
import org.freeplane.core.ui.IMouseWheelEventHandler;
import org.freeplane.view.swing.map.MapView;

/**
 * @author foltin
 */
public class MindMapMouseWheelEventHandler implements MouseWheelListener {
	private static final int HORIZONTAL_SCROLL_MASK = InputEvent.SHIFT_MASK
	        | InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK;
	private static int SCROLL_SKIPS = 8;
	private static final int ZOOM_MASK = InputEvent.CTRL_MASK;

	/**
	 *
	 */
	public MindMapMouseWheelEventHandler(final ModeController controller) {
		super();
		Controller.getResourceController().addPropertyChangeListener(
		    new IFreemindPropertyListener() {
			    public void propertyChanged(final String propertyName, final String newValue,
			                                final String oldValue) {
				    if (propertyName.equals(ResourceController.RESOURCES_WHEEL_VELOCITY)) {
					    MindMapMouseWheelEventHandler.SCROLL_SKIPS = Integer.parseInt(newValue);
				    }
			    }
		    });
		MindMapMouseWheelEventHandler.SCROLL_SKIPS = Controller.getResourceController()
		    .getIntProperty(ResourceController.RESOURCES_WHEEL_VELOCITY, 8);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freemind.modes.ModeController.MouseWheelEventHandler#handleMouseWheelEvent
	 * (java.awt.event.MouseWheelEvent)
	 */
	public void mouseWheelMoved(final MouseWheelEvent e) {
		final MapView mapView = (MapView) e.getSource();
		final ModeController mController = mapView.getModel().getModeController();
		if (mController.isBlocked()) {
			return;
		}
		final Set registeredMouseWheelEventHandler = mController.getMouseWheelEventHandlers();
		for (final Iterator i = registeredMouseWheelEventHandler.iterator(); i.hasNext();) {
			final IMouseWheelEventHandler handler = (IMouseWheelEventHandler) i.next();
			final boolean result = handler.handleMouseWheelEvent(e);
			if (result) {
				return;
			}
		}
		if ((e.getModifiers() & MindMapMouseWheelEventHandler.ZOOM_MASK) != 0) {
			float newZoomFactor = 1f + Math.abs((float) e.getWheelRotation()) / 10f;
			if (e.getWheelRotation() < 0) {
				newZoomFactor = 1 / newZoomFactor;
			}
			final float oldZoom = ((MapView) e.getComponent()).getZoom();
			float newZoom = oldZoom / newZoomFactor;
			newZoom = (float) Math.rint(newZoom * 1000f) / 1000f;
			newZoom = Math.max(1f / 32f, newZoom);
			newZoom = Math.min(32f, newZoom);
			if (newZoom != oldZoom) {
				Controller.getController().getViewController().setZoom(newZoom);
			}
		}
		else if ((e.getModifiers() & MindMapMouseWheelEventHandler.HORIZONTAL_SCROLL_MASK) != 0) {
			((MapView) e.getComponent()).scrollBy(MindMapMouseWheelEventHandler.SCROLL_SKIPS
			        * e.getWheelRotation(), 0);
		}
		else {
			((MapView) e.getComponent()).scrollBy(0, MindMapMouseWheelEventHandler.SCROLL_SKIPS
			        * e.getWheelRotation());
		}
	}
}
