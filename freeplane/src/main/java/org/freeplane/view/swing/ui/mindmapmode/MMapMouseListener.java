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
import java.lang.ref.WeakReference;

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
import org.freeplane.view.swing.map.link.ConnectorView;
import org.freeplane.view.swing.map.link.InclinationRecommender;
import org.freeplane.view.swing.ui.DefaultMapMouseListener;

/** */
public class MMapMouseListener extends DefaultMapMouseListener{
    ConnectorView connectorView = null;
	private Point connectorOldEndPoint;
	private Point connectorOldStartPoint;

	/**
	 *
	 */
	public MMapMouseListener() {
		super();
	}

	public void mouseDragged(final MouseEvent e) {
		final MapView mapView = (MapView) e.getComponent();
		if (connectorView == null || !mapView.getLayoutType().equals(MapViewLayout.MAP)) {
			super.mouseDragged(e);
			return;
		}
		ConnectorModel connector = connectorView.getModel();
		final NodeModel target = connector.getTarget();
		if(target == null) {
			super.mouseDragged(e);
			return;
		}
		final int deltaX = (int) ((e.getX() - originX) / mapView.getZoom());
		final int deltaY = (int) ((e.getY() - originY) / mapView.getZoom());
		double distSqToTarget = 0;
		double distSqToSource = 0;
		final NodeView targetView = mapView.getNodeView(target);
		final NodeView sourceView = mapView.getNodeView(connector.getSource());
		if (targetView != null && sourceView != null) {
			final Point targetLinkPoint = targetView.getLinkPoint(connector.getEndInclination());
			final Point sourceLinkPoint = sourceView.getLinkPoint(connector.getStartInclination());
			distSqToTarget = targetLinkPoint.distanceSq(originX, originY);
			distSqToSource = sourceLinkPoint.distanceSq(originX, originY);
		}
		if(connector.getStartInclination() == null || connector.getEndInclination() == null) {
		    InclinationRecommender recommender = new InclinationRecommender(linkController(), connectorView);
            if(connector.getStartInclination() == null)
                connector.setStartInclination(recommender.calcStartInclination());
            if(connector.getEndInclination() == null)
                connector.setEndInclination(recommender.calcEndInclination());
		}
		if ((targetView == null || sourceView != null) && distSqToSource <= distSqToTarget * 2.25) {
			final Point changedInclination = connector.getStartInclination();
			connector.changeInclination(deltaX, deltaY, connector.getSource(), changedInclination);
			connector.setStartInclination(changedInclination);
		}
		if ((sourceView == null || targetView != null) && distSqToTarget <= distSqToSource * 2.25) {
			final Point changedInclination = connector.getEndInclination();
			connector.changeInclination(deltaX, deltaY, target, changedInclination);
			connector.setEndInclination(changedInclination);
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
		final Object object = mapView.detectView(new Point(originX, originY));
		if (object instanceof ConnectorView) {
			connectorView = ((ConnectorView) object);
			ConnectorModel connector = connectorView.getModel();
			if(MapStyleModel.isDefaultStyleNode(connector.getSource()))
			    return;
			final Shape shape = linkController().getShape(connector);
			if (Shape.EDGE_LIKE.equals(shape)) {
				return;
			}
			connectorOldStartPoint = connector.getStartInclination();
			connectorOldEndPoint = connector.getEndInclination();
			connectorView.enableControlPoints();
			mapView.repaintVisible();
		}
	}

	public void mouseReleased(final MouseEvent e) {
		super.mouseReleased(e);
		if (connectorView != null) {
		    ConnectorModel connector = connectorView.getModel();
			final Point connectorNewStartPoint = connector.getStartInclination();
			final Point connectorNewEndPoint = connector.getEndInclination();
			connector.setStartInclination(connectorOldStartPoint);
			connector.setEndInclination(connectorOldEndPoint);
			linkController().setArrowLinkEndPoints(connector,
				connectorNewStartPoint, connectorNewEndPoint);
			connectorView.disableControlPoints();
			final MapView mapView = (MapView) e.getComponent();
			mapView.repaintVisible();
			connectorView = null;
		}
	}

     private MLinkController linkController() {
        return (MLinkController) LinkController.getController(Controller.getCurrentController().getModeController());
    }

	@Override
    public void mouseClicked(MouseEvent e) {

		if(e.getClickCount() == 2 && Compat.isCtrlEvent(e)){
			final MapView mapView = (MapView) e.getComponent();
			final Object object = mapView.detectView(new Point(originX, originY));
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
