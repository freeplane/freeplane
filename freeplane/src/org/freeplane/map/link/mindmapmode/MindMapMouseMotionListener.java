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
package org.freeplane.map.link.mindmapmode;

import java.awt.Point;
import java.awt.event.MouseEvent;

import org.freeplane.core.ui.IMapMouseReceiver;
import org.freeplane.map.link.ArrowLinkModel;
import org.freeplane.modes.mindmapmode.MModeController;
import org.freeplane.view.map.MapView;

/** */
class MindMapMouseMotionListener implements IMapMouseReceiver {
	ArrowLinkModel draggedLink = null;
	private Point draggedLinkOldEndPoint;
	private Point draggedLinkOldStartPoint;
	final private MModeController mController;
	int originX = -1;
	int originY = -1;

	/**
	 *
	 */
	public MindMapMouseMotionListener(final MModeController controller) {
		super();
		mController = controller;
	}

	public void mouseDragged(final MouseEvent e) {
		final MapView mapView = (MapView) e.getComponent();
		if (originX >= 0) {
			if (draggedLink != null) {
				final int deltaX = (int) ((e.getX() - originX) / mController.getMapView().getZoom());
				final int deltaY = (int) ((e.getY() - originY) / mController.getMapView().getZoom());
				draggedLink.changeInclination(mapView, originX, originY, deltaX, deltaY);
				originX = e.getX();
				originY = e.getY();
				mController.getMapView().repaint();
			}
			else {
				mapView.scrollBy(originX - e.getX(), originY - e.getY());
			}
		}
	}

	public void mousePressed(final MouseEvent e) {
		if (!mController.isBlocked() && e.getButton() == MouseEvent.BUTTON1) {
			mController.getMapView().setMoveCursor(true);
			originX = e.getX();
			originY = e.getY();
			draggedLink = mController.getMapView().detectCollision(new Point(originX, originY));
			if (draggedLink != null) {
				draggedLinkOldStartPoint = draggedLink.getStartInclination();
				draggedLinkOldEndPoint = draggedLink.getEndInclination();
				draggedLink.setShowControlPoints(true);
				mController.getMapView().repaint();
			}
		}
	}

	public void mouseReleased(final MouseEvent e) {
		originX = -1;
		originY = -1;
		if (draggedLink != null) {
			draggedLink.setShowControlPoints(false);
			final Point draggedLinkNewStartPoint = draggedLink.getStartInclination();
			final Point draggedLinkNewEndPoint = draggedLink.getEndInclination();
			draggedLink.setStartInclination(draggedLinkOldStartPoint);
			draggedLink.setEndInclination(draggedLinkOldEndPoint);
			((MLinkController) mController.getLinkController()).setArrowLinkEndPoints(draggedLink,
			    draggedLinkNewStartPoint, draggedLinkNewEndPoint);
			mController.getMapView().repaint();
			draggedLink = null;
		}
	}
}
