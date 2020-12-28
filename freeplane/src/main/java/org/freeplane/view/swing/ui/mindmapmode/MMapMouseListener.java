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

import javax.swing.JComponent;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.Connectors;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.ConnectorModel.Shape;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.FreeNode;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.MapViewLayout;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.ui.DefaultMapMouseListener;

/** */
public class MMapMouseListener extends DefaultMapMouseListener{
	ConnectorModel draggedLink = null;
	private Point draggedLinkOldEndPoint;
	private Point draggedLinkOldStartPoint;

	/**
	 *
	 */
	public MMapMouseListener() {
		super();
	}

	public void mouseDragged(final MouseEvent e) {
		final MapView mapView = (MapView) e.getComponent();
		if (draggedLink == null || !mapView.getLayoutType().equals(MapViewLayout.MAP)) {
			super.mouseDragged(e);
			return;
		}
		final NodeModel target = draggedLink.getTarget();
		if(target == null) {
			super.mouseDragged(e);
			return;
		}
		final int deltaX = (int) ((e.getX() - originX) / mapView.getZoom());
		final int deltaY = (int) ((e.getY() - originY) / mapView.getZoom());
		double distSqToTarget = 0;
		double distSqToSource = 0;
		final NodeView targetView = mapView.getNodeView(target);
		final NodeView sourceView = mapView.getNodeView(draggedLink.getSource());
		if (targetView != null && sourceView != null) {
			final Point targetLinkPoint = targetView.getLinkPoint(draggedLink.getEndInclination());
			final Point sourceLinkPoint = sourceView.getLinkPoint(draggedLink.getStartInclination());
			distSqToTarget = targetLinkPoint.distanceSq(originX, originY);
			distSqToSource = sourceLinkPoint.distanceSq(originX, originY);
		}
		if ((targetView == null || sourceView != null) && distSqToSource <= distSqToTarget * 2.25) {
			final Point changedInclination = draggedLink.getStartInclination();
			draggedLink.changeInclination(deltaX, deltaY, draggedLink.getSource(), changedInclination);
			draggedLink.setStartInclination(changedInclination);
		}
		if ((sourceView == null || targetView != null) && distSqToTarget <= distSqToSource * 2.25) {
			final Point changedInclination = draggedLink.getEndInclination();
			draggedLink.changeInclination(deltaX, deltaY, target, changedInclination);
			draggedLink.setEndInclination(changedInclination);
		}
		originX = e.getX();
		originY = e.getY();
		mapView.repaintVisible();

	}

	public void mousePressed(final MouseEvent e) {
		super.mousePressed(e);
		if(e.isPopupTrigger())
			return;
		final MapView mapView = (MapView) e.getComponent();
		if(mapView.getClientProperty(Connectors.class) != null)
			return;
		final Object object = mapView.detectCollision(new Point(originX, originY));
		if (object instanceof ConnectorModel) {
			final ConnectorModel arrowLinkModel = (ConnectorModel) object;
			if(MapStyleModel.DEFAULT_STYLE.equals(arrowLinkModel.getSource().getUserObject()))
			    return;
			final Shape shape = linkController().getShape(arrowLinkModel);
			if (Shape.EDGE_LIKE.equals(shape)) {
				return;
			}
			draggedLink = arrowLinkModel;
			draggedLinkOldStartPoint = draggedLink.getStartInclination();
			draggedLinkOldEndPoint = draggedLink.getEndInclination();
			draggedLink.setShowControlPoints(true);
			mapView.repaintVisible();
		}
	}

	public void mouseReleased(final MouseEvent e) {
		super.mouseReleased(e);
		if (draggedLink != null) {
			draggedLink.setShowControlPoints(false);
			final Point draggedLinkNewStartPoint = draggedLink.getStartInclination();
			final Point draggedLinkNewEndPoint = draggedLink.getEndInclination();
			draggedLink.setStartInclination(draggedLinkOldStartPoint);
			draggedLink.setEndInclination(draggedLinkOldEndPoint);
			linkController().setArrowLinkEndPoints(draggedLink,
				draggedLinkNewStartPoint, draggedLinkNewEndPoint);
			final MapView mapView = (MapView) e.getComponent();
			mapView.repaintVisible();
			draggedLink = null;
		}
	}

    private MLinkController linkController() {
        return (MLinkController) LinkController.getController(Controller.getCurrentController().getModeController());
    }

	@Override
    public void mouseClicked(MouseEvent e) {

		if(e.getClickCount() == 2 && Compat.isCtrlEvent(e)){
			final MapView mapView = (MapView) e.getComponent();
			final Object object = mapView.detectCollision(new Point(originX, originY));
			if(object != null)
				return;
			final ModeController modeController = Controller.getCurrentModeController();
			final IExtension freeNode = modeController.getExtension(FreeNode.class);
			if(freeNode != null && modeController instanceof MModeController){
				final JComponent rootContent = mapView.getRoot().getMainView();
				final Point contentPt = new Point();
				UITools.convertPointToAncestor(rootContent, contentPt, mapView);
				final float zoom = mapView.getZoom();
				final Point eventPoint = e.getPoint();
				int x =(int) ((eventPoint.x - contentPt.x)/zoom);
				final int y =(int) ((eventPoint.y - contentPt.y)/zoom);
				final int rootContentNormalWidth = (int)(rootContent.getWidth()/zoom);
				final boolean newNodeIsLeft = x < rootContentNormalWidth/2;
				if(newNodeIsLeft){
					x = rootContentNormalWidth - x;
				}
				final Point pt = new Point(x, y);
				((MMapController)modeController.getMapController()).addFreeNode(pt, newNodeIsLeft);
			}
		}
		else
			super.mouseClicked(e);
    }
	
	
}
