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
package org.freeplane.map.nodelocation.mindmapmode;

import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.freeplane.controller.Controller;
import org.freeplane.main.Tools;
import org.freeplane.map.nodelocation.LocationModel;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.view.MapView;
import org.freeplane.map.tree.view.NodeMotionListenerView;
import org.freeplane.map.tree.view.NodeView;
import org.freeplane.modes.UserInputListenerFactory;
import org.freeplane.modes.mindmapmode.MModeController;

/**
 * The MouseMotionListener which belongs to every NodeView
 */
class MNodeMotionListener extends UserInputListenerFactory.DefaultNodeMotionListener {
	final private MModeController c;
	private Point dragStartingPoint = null;
	private int originalHGap;
	private int originalParentVGap;
	private int originalShiftY;

	public MNodeMotionListener(final MModeController controller) {
		c = controller;
	}

	Point getDragStartingPoint() {
		return dragStartingPoint;
	}

	/**
	 */
	private int getHGap(final Point dragNextPoint, final NodeModel node,
	                    final Point dragStartingPoint) {
		int oldHGap = node.getLocationModel().getHGap();
		int hGapChange = (int) ((dragNextPoint.x - dragStartingPoint.x) / c.getMapView().getZoom());
		if (node.isLeft()) {
			hGapChange = -hGapChange;
		}
		oldHGap += +hGapChange;
		return oldHGap;
	}

	/**
	 */
	private int getNodeShiftY(final Point dragNextPoint, final NodeModel node,
	                          final Point dragStartingPoint) {
		int shiftY = node.getLocationModel().getShiftY();
		final int shiftYChange = (int) ((dragNextPoint.y - dragStartingPoint.y) / c.getMapView()
		    .getZoom());
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
	private int getVGap(final Point dragNextPoint, final NodeModel node,
	                    final Point dragStartingPoint) {
		int oldVGap = node.getLocationModel().getVGap();
		final int vGapChange = (int) ((dragNextPoint.y - dragStartingPoint.y) / c.getMapView()
		    .getZoom());
		oldVGap = Math.max(0, oldVGap - vGapChange);
		return oldVGap;
	}

	public boolean isActive() {
		return getDragStartingPoint() != null;
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		if (e.getButton() == 1 && e.getClickCount() == 2) {
			if (e.getModifiersEx() == 0) {
				final NodeView nodeV = getNodeView(e);
				final NodeModel node = nodeV.getModel();
				c.moveNodePosition(node, node.getLocationModel().getVGap(), LocationModel.HGAP, 0);
				return;
			}
			if (e.getModifiersEx() == InputEvent.CTRL_DOWN_MASK) {
				final NodeView nodeV = getNodeView(e);
				final NodeModel node = nodeV.getModel();
				c.moveNodePosition(node, LocationModel.VGAP, node.getLocationModel().getHGap(),
				    node.getLocationModel().getShiftY());
				return;
			}
		}
	}

	/** Invoked when a mouse button is pressed on a component and then dragged. */
	@Override
	public void mouseDragged(final MouseEvent e) {
		if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) == (InputEvent.BUTTON1_DOWN_MASK)) {
			final NodeMotionListenerView motionListenerView = (NodeMotionListenerView) e
			    .getSource();
			final NodeView nodeV = getNodeView(e);
			final MapView mapView = nodeV.getMap();
			final Point point = e.getPoint();
			Tools.convertPointToAncestor(motionListenerView, point, JScrollPane.class);
			if (!isActive()) {
				setDragStartingPoint(point, nodeV.getModel());
			}
			else {
				final Point dragNextPoint = point;
				if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == 0) {
					final NodeModel node = nodeV.getModel();
					final LocationModel locationModel = node.createLocationModel();
					locationModel.setShiftY(getNodeShiftY(dragNextPoint, node, dragStartingPoint));
					locationModel.setHGap(getHGap(dragNextPoint, node, dragStartingPoint));
					c.getMapController().nodeRefresh(node);
				}
				else {
					final NodeModel parentNode = nodeV.getVisibleParentView().getModel();
					parentNode.createLocationModel().setVGap(
					    getVGap(dragNextPoint, parentNode, dragStartingPoint));
					Controller.getController().getMap().nodeRefresh(parentNode);
					Controller.getController().getMap().nodeRefresh(nodeV.getModel());
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

	@Override
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
		Tools.convertPointToAncestor(nodeV, point, JScrollPane.class);
		final NodeModel node = nodeV.getModel();
		final NodeModel parentNode = nodeV.getModel().getParentNode();
		final int parentVGap = parentNode.getLocationModel().getVGap();
		final int hgap = node.getLocationModel().getHGap();
		final int shiftY = node.getLocationModel().getShiftY();
		resetPositions(node);
		c.moveNodePosition(node, parentVGap, hgap, shiftY);
		stopDrag();
	}

	/**
	 */
	private void resetPositions(final NodeModel node) {
		final LocationModel locationModel = node.getParentNode().getLocationModel();
		locationModel.setVGap(originalParentVGap);
		node.getLocationModel().setHGap(originalHGap);
		node.getLocationModel().setShiftY(originalShiftY);
	}

	void setDragStartingPoint(final Point point, final NodeModel node) {
		dragStartingPoint = point;
		if (point != null) {
			originalParentVGap = node.getParentNode().getLocationModel().getVGap();
			originalHGap = node.getLocationModel().getHGap();
			originalShiftY = node.getLocationModel().getShiftY();
		}
		else {
			originalParentVGap = originalHGap = originalShiftY = 0;
		}
	}

	private void stopDrag() {
		setDragStartingPoint(null, null);
	}
}
