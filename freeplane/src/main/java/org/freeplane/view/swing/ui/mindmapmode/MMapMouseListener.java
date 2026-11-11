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
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Objects;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.ConnectorShape;
import org.freeplane.features.link.Connectors;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.FreeNode;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeModel.Side;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.styles.MapViewLayout;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.map.link.ConnectorEnd;
import org.freeplane.view.swing.map.link.ConnectorTangent;
import org.freeplane.view.swing.map.link.ConnectorView;
import org.freeplane.view.swing.map.link.InclinationRecommender;
import org.freeplane.view.swing.ui.DefaultMapMouseListener;

/** */
public class MMapMouseListener extends DefaultMapMouseListener{
    ConnectorView connectorView = null;
	private ConnectorEnd draggedControlPoint;
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
		ConnectorModel connector = connectorView.getConnector();
		final NodeModel target = connector.getTarget();
		if(target == null) {
			super.mouseDragged(e);
			return;
		}
		if(draggedControlPoint != null) {
			dragControlPoint(e, mapView, connector);
			return;
		}
		if(originX == -1)
			return;
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
		initializeMissingInclinations(connector);
		if ((targetView == null || sourceView != null) && distSqToSource <= distSqToTarget * 2.25) {
			final Point changedInclination = connector.getStartInclination();
			connector.changeInclination(deltaX, deltaY, mapView.getRoot().getNode(), connector.getSource(), changedInclination);
			connector.setStartInclination(changedInclination);
		}
		if ((sourceView == null || targetView != null) && distSqToTarget <= distSqToSource * 2.25) {
			final Point changedInclination = connector.getEndInclination();
			connector.changeInclination(deltaX, deltaY, mapView.getRoot().getNode(), target, changedInclination);
			connector.setEndInclination(changedInclination);
		}
		originX = e.getX();
		originY = e.getY();
		mapView.repaintVisible();

	}

	private void dragControlPoint(final MouseEvent e, final MapView mapView, ConnectorModel connector) {
		initializeMissingInclinations(connector);
		final boolean dragsSource = draggedControlPoint == ConnectorEnd.SOURCE;
		final NodeView nodeView = mapView.getNodeView(dragsSource ? connector.getSource() : connector.getTarget());
		if (nodeView == null)
			return;
		final Point oldInclination = dragsSource ? connectorOldStartPoint : connectorOldEndPoint;
		final Point currentInclination = dragsSource ? connector.getStartInclination() : connector.getEndInclination();
		final ConnectorTangent tangent = new ConnectorTangent(contentBounds(mapView, nodeView), mapView.getZoom(), nodeView.isTopOrLeft())
				.snappingAngle(e.isShiftDown());
		if (Compat.isCtrlEvent(e))
			tangent.keepingLengthOf(oldInclination != null ? oldInclination : currentInclination);
		final Point inclination = tangent.inclinationWithHandleAt(e.getPoint(), currentInclination);
		if (dragsSource)
			connector.setStartInclination(inclination);
		else
			connector.setEndInclination(inclination);
		mapView.repaintVisible();
		e.consume();
	}

	private Rectangle contentBounds(final MapView mapView, final NodeView nodeView) {
		final JComponent content = nodeView.getContent();
		final Point location = content.getLocation();
		UITools.convertPointToAncestor(nodeView, location, mapView);
		return new Rectangle(location.x, location.y, content.getWidth(), content.getHeight());
	}

	private void initializeMissingInclinations(ConnectorModel connector) {
		if(connector.getStartInclination() == null || connector.getEndInclination() == null) {
		    InclinationRecommender recommender = new InclinationRecommender(linkController(), connectorView);
            if(connector.getStartInclination() == null)
                connector.setStartInclination(recommender.calcStartInclination());
            if(connector.getEndInclination() == null)
                connector.setEndInclination(recommender.calcEndInclination());
		}
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		super.mousePressed(e);
		if(Compat.isPopupTrigger(e))
			return;
		if(! SwingUtilities.getWindowAncestor(e.getComponent()).isFocused())
			return;
		final MapView mapView = (MapView) e.getComponent();
		if (! mapView.isValid())
			return;
		if(mapView.getClientProperty(Connectors.class) != null)
			return;
		if(e.getButton() != MouseEvent.BUTTON1)
			return;
		final ConnectorView controlPointOwner = mapView.getLayoutType().equals(MapViewLayout.MAP)
				? mapView.detectConnectorControlPoint(e.getPoint()) : null;
		if (controlPointOwner != null) {
			connectorView = controlPointOwner;
			draggedControlPoint = controlPointOwner.controlPointAt(e.getPoint());
			connectorOldStartPoint = connectorView.getConnector().getStartInclination();
			connectorOldEndPoint = connectorView.getConnector().getEndInclination();
			return;
		}
		final Object object = mapView.detectView(e.getPoint());
		if (object instanceof ConnectorView) {
			connectorView = ((ConnectorView) object);
			ConnectorModel connector = connectorView.getConnector();
			final ConnectorShape shape = linkController().getShape(connector);
			if (ConnectorShape.EDGE_LIKE.equals(shape)) {
				return;
			}
			connectorOldStartPoint = connector.getStartInclination();
			connectorOldEndPoint = connector.getEndInclination();
			connectorView.enableControlPoints();
			SelectedConnectorListener.install(mapView);
			mapView.repaintVisible();
		}
		else if (ConnectorView.disableControlPoints(mapView))
			mapView.repaintVisible();
	}

	public void mouseReleased(final MouseEvent e) {
		super.mouseReleased(e);
		if (connectorView != null) {
		    ConnectorModel connector = connectorView.getConnector();
			final Point connectorNewStartPoint = connector.getStartInclination();
			final Point connectorNewEndPoint = connector.getEndInclination();
			connector.setStartInclination(connectorOldStartPoint);
			connector.setEndInclination(connectorOldEndPoint);
			if (! Objects.equals(connectorNewStartPoint, connectorOldStartPoint)
					|| ! Objects.equals(connectorNewEndPoint, connectorOldEndPoint))
				linkController().setArrowLinkEndPoints(connector,
					connectorNewStartPoint, connectorNewEndPoint);
			final MapView mapView = (MapView) e.getComponent();
			mapView.repaintVisible();
			connectorView = null;
			draggedControlPoint = null;
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
				NodeView root = mapView.getRoot();
				final JComponent rootContent = root.getMainView();
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
				((MMapController)modeController.getMapController()).addFreeNode(root.getNode(), pt, newNodeIsLeft ? Side.TOP_OR_LEFT : Side.BOTTOM_OR_RIGHT);
			}
		}
		else if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1 && revealOtherEnd(e))
			e.consume();
		else
			super.mouseClicked(e);
    }

	private boolean revealOtherEnd(MouseEvent e) {
		final MapView mapView = (MapView) e.getComponent();
		final Object object = mapView.detectView(e.getPoint());
		if (!(object instanceof ConnectorView))
			return false;
		final ConnectorView view = (ConnectorView) object;
		final ConnectorEnd end = view.endToRevealFrom(e.getPoint());
		if (end == null)
			return false;
		final ConnectorModel connector = view.getConnector();
		final NodeModel node = end == ConnectorEnd.SOURCE ? connector.getSource() : connector.getTarget();
		if (node == null)
			return false;
		linkController().goToLinkedNode(node);
		return true;
	}


}
