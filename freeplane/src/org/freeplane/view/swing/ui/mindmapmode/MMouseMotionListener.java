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

import java.awt.Point;
import java.awt.event.MouseEvent;

import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.IMapMouseReceiver;
import org.freeplane.features.common.addins.mapstyle.MapViewLayout;
import org.freeplane.features.common.link.ConnectorModel;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.mindmapmode.link.MLinkController;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

/** */
public class MMouseMotionListener implements IMapMouseReceiver {
	ConnectorModel draggedLink = null;
	private Point draggedLinkOldEndPoint;
	private Point draggedLinkOldStartPoint;
	final private ModeController mController;
	int originX = -1;
	int originY = -1;

	/**
	 *
	 */
	public MMouseMotionListener(final ModeController controller) {
		super();
		mController = controller;
	}

	public void mouseDragged(final MouseEvent e) {
		final MapView mapView = (MapView) e.getComponent();
		if (originX >= 0) {
			if (draggedLink != null && mapView.getLayoutType().equals(MapViewLayout.MAP)) {
				final int deltaX = (int) ((e.getX() - originX) / mapView.getZoom());
				final int deltaY = (int) ((e.getY() - originY) / mapView.getZoom());
				double distSqToTarget = 0;
				double distSqToSource = 0;
				final NodeModel target = draggedLink.getTarget();
				final NodeView targetView = mapView.getNodeView(target);
				final NodeView sourceView = mapView.getNodeView(draggedLink.getSource());
				if (targetView != null && sourceView != null) {
					final Point targetLinkPoint = targetView.getLinkPoint(draggedLink.getEndInclination());
					final Point sourceLinkPoint = sourceView.getLinkPoint(draggedLink.getStartInclination());
					distSqToTarget = targetLinkPoint.distanceSq(originX, originY);
					distSqToSource = sourceLinkPoint.distanceSq(originX, originY);
				}
				if ((targetView == null || sourceView != null) && distSqToSource < distSqToTarget * 2.25) {
					final Point changedInclination = draggedLink.getStartInclination();
					draggedLink.changeInclination(deltaX, deltaY, draggedLink.getSource(), changedInclination);
					draggedLink.setStartInclination(changedInclination);
				}
				if ((sourceView == null || targetView != null) && distSqToTarget < distSqToSource * 2.25) {
					final Point changedInclination = draggedLink.getEndInclination();
					draggedLink.changeInclination(deltaX, deltaY, target, changedInclination);
					draggedLink.setEndInclination(changedInclination);
				}
				originX = e.getX();
				originY = e.getY();
				mapView.repaintVisible();
			}
			else {
				mapView.scrollBy(originX - e.getX(), originY - e.getY());
			}
		}
	}

	public void mousePressed(final MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			final MapView mapView = (MapView) e.getComponent();
			mapView.setMoveCursor(true);
			originX = e.getX();
			originY = e.getY();
			final Object object = mapView.detectCollision(new Point(originX, originY));
			if (object instanceof ConnectorModel) {
				final ConnectorModel arrowLinkModel = (ConnectorModel) object;
				if (arrowLinkModel.isEdgeLike()) {
					return;
				}
				draggedLink = arrowLinkModel;
				draggedLinkOldStartPoint = draggedLink.getStartInclination();
				draggedLinkOldEndPoint = draggedLink.getEndInclination();
				draggedLink.setShowControlPoints(true);
				mapView.repaintVisible();
			}
		}
	}

	public void mouseReleased(final MouseEvent e) {
		originX = -1;
		originY = -1;
		if (draggedLink != null) {
			final MapView mapView = (MapView) e.getComponent();
			draggedLink.setShowControlPoints(false);
			final Point draggedLinkNewStartPoint = draggedLink.getStartInclination();
			final Point draggedLinkNewEndPoint = draggedLink.getEndInclination();
			draggedLink.setStartInclination(draggedLinkOldStartPoint);
			draggedLink.setEndInclination(draggedLinkOldEndPoint);
			((MLinkController) LinkController.getController(mController)).setArrowLinkEndPoints(draggedLink,
			    draggedLinkNewStartPoint, draggedLinkNewEndPoint);
			mapView.repaintVisible();
			draggedLink = null;
		}
	}
}
