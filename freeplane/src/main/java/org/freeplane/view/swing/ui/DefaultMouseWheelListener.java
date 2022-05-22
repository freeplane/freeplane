package org.freeplane.view.swing.ui;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IMouseWheelEventHandler;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.view.swing.map.MapView;

/**
 * @author foltin
 */
public class DefaultMouseWheelListener implements MouseWheelListener {
	private static final String ZOOM_AROUND_SELECTED_NODE_PROPERTY = "zoomAroundSelectedNode";
	private static final int ZOOM_MASK = InputEvent.CTRL_MASK;
// // 	final private Controller controller;

	/**
	 *
	 */
	public DefaultMouseWheelListener() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.modes.ModeController.MouseWheelEventHandler#handleMouseWheelEvent
	 * (java.awt.event.MouseWheelEvent)
	 */
	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		final MapView mapView = (MapView) e.getSource();
		final ModeController mController = mapView.getModeController();
		if (mController.isBlocked() || Controller.getCurrentController().getMap() != mapView.getModel())
			return;
		final Set<IMouseWheelEventHandler> registeredMouseWheelEventHandler = mController.getUserInputListenerFactory()
		    .getMouseWheelEventHandlers();
		for (final IMouseWheelEventHandler handler : registeredMouseWheelEventHandler) {
			final boolean result = handler.handleMouseWheelEvent(e);
			if (result) {
				return;
			}
		}
		if ((e.getModifiers() & DefaultMouseWheelListener.ZOOM_MASK) != 0) {
			float newZoomFactor = 1f + Math.abs((float) e.getUnitsToScroll()) / 10f;
			if (e.getUnitsToScroll() < 0) {
				newZoomFactor = 1 / newZoomFactor;
			}
			final float oldZoom = ((MapView) e.getComponent()).getZoom();
			float newZoom = oldZoom / newZoomFactor;
			newZoom = (float) Math.rint(newZoom * 1000f) / 1000f;
			newZoom = Math.max(1f / 32f, newZoom);
			newZoom = Math.min(32f, newZoom);
			if (newZoom != oldZoom) {
			    if(! ResourceController.getResourceController().getBooleanProperty(ZOOM_AROUND_SELECTED_NODE_PROPERTY))
			        mapView.setZoom(newZoom, e.getPoint());
				Controller.getCurrentController().getMapViewManager().setZoom(newZoom);
			}
		}
		else {
			JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, mapView);
			final Point location = new Point(e.getX(), e.getY());
			UITools.convertPointToAncestor(mapView, location, scrollPane);
			final MouseWheelEvent mapWheelEvent = new MouseWheelEvent(scrollPane, e.getID(), e.getWhen(), e.getModifiers() | e.getModifiersEx(),
					location.x, location.y, e.getXOnScreen(), e.getYOnScreen(),
					e.getClickCount(), e.isPopupTrigger(), e.getScrollType(),
					e.getScrollAmount(), e.getWheelRotation());
			if(scrollPane != null) {
				for (MouseWheelListener l : scrollPane.getMouseWheelListeners())
					l.mouseWheelMoved(mapWheelEvent);
			}
		}
	}
}
