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
package org.freeplane.view.swing.ui.mindmapmode;

import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.IMouseListener;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.common.nodelocation.LocationController;
import org.freeplane.features.common.nodelocation.LocationModel;
import org.freeplane.features.mindmapmode.nodelocation.MLocationController;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeMotionListenerView;
import org.freeplane.view.swing.map.NodeView;

/**
 * The MouseMotionListener which belongs to every NodeView
 */
public class MNodeMotionListener extends MouseAdapter implements IMouseListener {
	final private ModeController c;
	private Point dragStartingPoint = null;
	private int originalHGap;
	private int originalParentVGap;
	private int originalShiftY;

	public MNodeMotionListener(final ModeController controller) {
		c = controller;
	}

	Point getDragStartingPoint() {
		return dragStartingPoint;
	}

	/**
	 */
	private int getHGap(final Point dragNextPoint, final NodeModel node, final Point dragStartingPoint) {
		int oldHGap = LocationModel.getModel(node).getHGap();
		final Controller controller = c.getController();
		final MapView mapView = ((MapView) controller.getViewController().getMapView());
		int hGapChange = (int) ((dragNextPoint.x - dragStartingPoint.x) / mapView.getZoom());
		if (node.isLeft()) {
			hGapChange = -hGapChange;
		}
		oldHGap += +hGapChange;
		return oldHGap;
	}

	/**
	 */
	private int getNodeShiftY(final Point dragNextPoint, final NodeModel node, final Point dragStartingPoint) {
		int shiftY = LocationModel.getModel(node).getShiftY();
		final Controller controller = c.getController();
		final MapView mapView = ((MapView) controller.getViewController().getMapView());
		final int shiftYChange = (int) ((dragNextPoint.y - dragStartingPoint.y) / mapView.getZoom());
		shiftY += shiftYChange;
		return shiftY;
	}

	/**
	 */
	private NodeView getNodeView(final MouseEvent e) {
		return ((NodeMotionListenerView) e.getSource()).getMovedView();
	}

	/**
	 */
	private int getVGap(final Point dragNextPoint, final NodeModel node, final Point dragStartingPoint) {
		int oldVGap = LocationModel.getModel(node).getVGap();
		final Controller controller = c.getController();
		final MapView mapView = ((MapView) controller.getViewController().getMapView());
		final int vGapChange = (int) ((dragNextPoint.y - dragStartingPoint.y) / mapView.getZoom());
		oldVGap = Math.max(0, oldVGap - vGapChange);
		return oldVGap;
	}

	public boolean isActive() {
		return dragStartingPoint != null;
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		if (e.getButton() == 1 && e.getClickCount() == 2) {
			if (e.getModifiersEx() == 0) {
				final NodeView nodeV = getNodeView(e);
				final NodeModel node = nodeV.getModel();
				((MLocationController) LocationController.getController(c)).moveNodePosition(node, LocationModel
				    .getModel(node).getVGap(), LocationModel.HGAP, 0);
				return;
			}
			if (e.getModifiersEx() == InputEvent.CTRL_DOWN_MASK) {
				final NodeView nodeV = getNodeView(e);
				final NodeModel node = nodeV.getModel();
				((MLocationController) LocationController.getController(c)).moveNodePosition(node, LocationModel.VGAP,
				    LocationModel.getModel(node).getHGap(), LocationModel.getModel(node).getShiftY());
				return;
			}
		}
	}

	/** Invoked when a mouse button is pressed on a component and then dragged. */
	public void mouseDragged(final MouseEvent e) {
		if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) == (InputEvent.BUTTON1_DOWN_MASK)) {
			final NodeMotionListenerView motionListenerView = (NodeMotionListenerView) e.getSource();
			final NodeView nodeV = getNodeView(e);
			final MapView mapView = nodeV.getMap();
			final Point point = e.getPoint();
			UITools.convertPointToAncestor(motionListenerView, point, JScrollPane.class);
			if (!isActive()) {
				setDragStartingPoint(point, nodeV.getModel());
			}
			else {
				final Point dragNextPoint = point;
				if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == 0) {
					final NodeModel node = nodeV.getModel();
					final LocationModel locationModel = LocationModel.createLocationModel(node);
					locationModel.setShiftY(getNodeShiftY(dragNextPoint, node, dragStartingPoint));
					locationModel.setHGap(getHGap(dragNextPoint, node, dragStartingPoint));
					c.getMapController().nodeRefresh(node);
				}
				else {
					final NodeModel parentNode = nodeV.getVisibleParentView().getModel();
					LocationModel.createLocationModel(parentNode).setVGap(
					    getVGap(dragNextPoint, parentNode, dragStartingPoint));
					final MapController mapController = c.getMapController();
					mapController.nodeRefresh(parentNode);
					mapController.nodeRefresh(nodeV.getModel());
				}
				dragStartingPoint = point;
			}
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					final Rectangle r = motionListenerView.getBounds();
					final boolean isEventPointVisible = mapView.getVisibleRect().contains(r);
					if (!isEventPointVisible) {
						mapView.scrollRectToVisible(r);
					}
				}
			});
		}
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
		if (!JOptionPane.getFrameForComponent(e.getComponent()).isFocused()) {
			return;
		}
		if (!isActive()) {
			final NodeMotionListenerView v = (NodeMotionListenerView) e.getSource();
			v.setMouseEntered();
		}
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		if (!isActive()) {
			final NodeMotionListenerView v = (NodeMotionListenerView) e.getSource();
			v.setMouseExited();
		}
	}

	public void mouseMoved(final MouseEvent e) {
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		final NodeMotionListenerView v = (NodeMotionListenerView) e.getSource();
		if (!v.contains(e.getX(), e.getY())) {
			v.setMouseExited();
		}
		if (!isActive()) {
			return;
		}
		final NodeView nodeV = getNodeView(e);
		final Point point = e.getPoint();
		UITools.convertPointToAncestor(nodeV, point, JScrollPane.class);
		final NodeModel node = nodeV.getModel();
		final NodeModel parentNode = nodeV.getModel().getParentNode();
		final int parentVGap = LocationModel.getModel(parentNode).getVGap();
		final int hgap = LocationModel.getModel(node).getHGap();
		final int shiftY = LocationModel.getModel(node).getShiftY();
		resetPositions(node);
		((MLocationController) LocationController.getController(c)).moveNodePosition(node, parentVGap, hgap, shiftY);
		stopDrag();
	}

	/**
	 */
	private void resetPositions(final NodeModel node) {
		final LocationModel locationModel = LocationModel.getModel(node.getParentNode());
		locationModel.setVGap(originalParentVGap);
		LocationModel.getModel(node).setHGap(originalHGap);
		LocationModel.getModel(node).setShiftY(originalShiftY);
	}

	void setDragStartingPoint(final Point point, final NodeModel node) {
		dragStartingPoint = point;
		if (point != null) {
			originalParentVGap = LocationModel.getModel(node.getParentNode()).getVGap();
			originalHGap = LocationModel.getModel(node).getHGap();
			originalShiftY = LocationModel.getModel(node).getShiftY();
		}
		else {
			originalParentVGap = originalHGap = originalShiftY = 0;
		}
	}

	private void stopDrag() {
		setDragStartingPoint(null, null);
	}
}
