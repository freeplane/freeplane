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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IMouseListener;
import org.freeplane.core.ui.IEditHandler.FirstAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.features.map.FreeNode;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNode;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodelocation.LocationController;
import org.freeplane.features.nodelocation.LocationModel;
import org.freeplane.features.nodelocation.mindmapmode.MLocationController;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.MouseArea;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.ui.DefaultNodeMouseMotionListener;

/**
 * The MouseMotionListener which belongs to every NodeView
 */
public class MNodeMotionListener extends DefaultNodeMouseMotionListener implements IMouseListener {
	private Point dragStartingPoint = null;
	private int originalHGap;
	private int originalParentVGap;
	private int originalShiftY;

	public MNodeMotionListener() {
	}

	Point getDragStartingPoint() {
		return dragStartingPoint;
	}

	/**
	 */
	private int getHGap(final Point dragNextPoint, final NodeModel node, final Point dragStartingPoint) {
		int oldHGap = LocationModel.getModel(node).getHGap();
		final Controller controller = Controller.getCurrentController();
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
		final Controller controller = Controller.getCurrentController();
		final MapView mapView = ((MapView) controller.getViewController().getMapView());
		final int shiftYChange = (int) ((dragNextPoint.y - dragStartingPoint.y) / mapView.getZoom());
		shiftY += shiftYChange;
		return shiftY;
	}

	/**
	 */
	private NodeView getNodeView(final MouseEvent e) {
		return ((MainView) e.getSource()).getNodeView();
	}

	/**
	 */
	private int getVGap(final Point dragNextPoint, final NodeModel node, final Point dragStartingPoint) {
		int oldVGap = LocationModel.getModel(node).getVGap();
		final Controller controller = Controller.getCurrentController();
		final MapView mapView = ((MapView) controller.getViewController().getMapView());
		final int vGapChange = (int) ((dragNextPoint.y - dragStartingPoint.y) / mapView.getZoom());
		oldVGap = Math.max(0, oldVGap - vGapChange);
		return oldVGap;
	}

	public boolean isDragActive() {
		return dragStartingPoint != null;
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
	    	final MainView mainView = (MainView)e.getComponent();
			if ( ! mainView.getMouseArea().equals(MouseArea.MOTION) && Compat.isPlainEvent(e) 
	    			&& ! mainView.isInFoldingRegion(e.getX())) {
	    		final MTextController textController = (MTextController) MTextController.getController();
	    		textController.getEventQueue().activate(e);
	    		textController.edit(FirstAction.EDIT_CURRENT, false);
	    	}
			final Controller controller = Controller.getCurrentController();
			MLocationController locationController = (MLocationController) LocationController.getController(controller.getModeController());
			if (e.getModifiersEx() == 0) {
				final NodeView nodeV = getNodeView(e);
				final NodeModel node = nodeV.getModel();
				locationController.moveNodePosition(node, LocationModel
				    .getModel(node).getVGap(), LocationModel.HGAP, 0);
				return;
			}
			if (Compat.isCtrlEvent(e)) {
				final NodeView nodeV = getNodeView(e);
				final NodeModel node = nodeV.getModel();
				locationController.moveNodePosition(node, LocationModel.VGAP,
				    LocationModel.getModel(node).getHGap(), LocationModel.getModel(node).getShiftY());
				return;
			}
		}
		super.mouseClicked(e);
	}
	
	public void mouseMoved(final MouseEvent e) {
		if (isDragActive())
			return;
		final MainView v = (MainView) e.getSource();
		final MouseArea mouseArea = v.whichMouseArea(e.getPoint());
		if(mouseArea.equals(MouseArea.MOTION)){
			v.setMouseArea(mouseArea);
			v.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			return;
		}
		v.setMouseArea(MouseArea.DEFAULT);
		super.mouseMoved(e);
	}


	@Override
	public void mouseExited(MouseEvent e) {
		if(! isDragActive())
			super.mouseExited(e);
	}

	/** Invoked when a mouse button is pressed on a component and then dragged. */
	public void mouseDragged(final MouseEvent e) {
		if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) == (InputEvent.BUTTON1_DOWN_MASK)) {
			stopTimerForDelayedSelection();
			final MainView motionListenerView = (MainView) e.getSource();
			final NodeView nodeV = getNodeView(e);
			final MapView mapView = nodeV.getMap();
			final Point point = e.getPoint();
			findGridPoint(point);
			UITools.convertPointToAncestor(nodeV, point, JScrollPane.class);
			if (!isDragActive()) {
				setDragStartingPoint(point, nodeV.getModel());
			}
			else {
				ModeController c = Controller.getCurrentController().getModeController();
				final Point dragNextPoint = point;
				if (! Compat.isCtrlEvent(e)) {
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
					UITools.convertRectangleToAncestor(motionListenerView, r, mapView);
					final boolean isEventPointVisible = mapView.getVisibleRect().contains(r);
					if (!isEventPointVisible) {
						mapView.scrollRectToVisible(r);
					}
				}
			});
		}
	}

	private void findGridPoint(Point point) {
	    final int gridSize = ResourceController.getResourceController().getIntProperty("grid_size");
	    if(gridSize <= 2){
	    	return;
	    }
	    point.x  -= point.x % gridSize;
	    point.y  -= point.y % gridSize;
    }

	@Override
	public void mouseReleased(final MouseEvent e) {
		final MainView v = (MainView) e.getSource();
		if (!v.contains(e.getX(), e.getY())) {
			v.setMouseArea(MouseArea.DEFAULT);
		}
		if (!isDragActive()) {
			super.mouseReleased(e);
			return;
		}
		final NodeView nodeV = getNodeView(e);
		final NodeModel node = nodeV.getModel();
		final ModeController modeController = nodeV.getMap().getModeController();
		final NodeModel parentNode = nodeV.getModel().getParentNode();
		final int parentVGap = LocationModel.getModel(parentNode).getVGap();
		int hgap = LocationModel.getModel(node).getHGap();
		final int shiftY = LocationModel.getModel(node).getShiftY();
		adjustNodeIndices(nodeV);
		resetPositions(node);
		final Controller controller = modeController.getController();
		MLocationController locationController = (MLocationController) LocationController.getController(controller.getModeController());
		locationController.moveNodePosition(node, parentVGap, hgap, shiftY);
		stopDrag();
	}

	private void adjustNodeIndices(final NodeView nodeV) {
		NodeModel[] selectedsBackup = null; 
		final NodeModel node = nodeV.getModel();
		if(FreeNode.isFreeNode(node)){
			selectedsBackup = adjustNodeIndexBackupSelection(nodeV, selectedsBackup);
		}
		else{
			final MapView map = nodeV.getMap();
			final NodeModel[] siblingNodes = node.getParentNode().getChildren().toArray(new NodeModel[]{});
			for(NodeModel sibling : siblingNodes){
				if(FreeNode.isFreeNode(sibling)){
					final NodeView siblingV = map.getNodeView(sibling);
					selectedsBackup = adjustNodeIndexBackupSelection(siblingV, selectedsBackup);
				}
			}
		}
		if(selectedsBackup != null){
			final ModeController modeController = nodeV.getMap().getModeController();
			final Controller controller = modeController.getController();
			controller.getSelection().replaceSelection(selectedsBackup);
		}
    }

	private NodeModel[] adjustNodeIndexBackupSelection(final NodeView nodeV, NodeModel[] selectedsBackup) {
		final NodeModel node = nodeV.getModel();
	    boolean isLeft = nodeV.isLeft();
	    final int newIndex = calculateNewNodeIndex(nodeV, isLeft, 0, node.getParentNode().getChildCount());
	    if(newIndex != -1){
	    	final ModeController modeController = nodeV.getMap().getModeController();
	    	MMapController mapController = (MMapController) modeController.getMapController();
	    	if(selectedsBackup == null){
		    	final Collection<NodeModel> selecteds = mapController.getSelectedNodes();
	    		selectedsBackup = selecteds.toArray(new NodeModel[selecteds.size()]);
	    	}
	    	mapController.moveNode(node, node.getParentNode(),  newIndex, isLeft, false);
	    }
	    return selectedsBackup;
    }

	public int getRefX(final NodeView node) {
	    return node.getContent().getX() + node.getContent().getWidth()/2;
    }

	private int calculateNewNodeIndex(final NodeView nodeV, final boolean left, final int start, final int end) {
		final NodeModel node = nodeV.getModel();
		if(SummaryNode.isSummaryNode(node))
			return -1;
		final int nodeY = getRefY(nodeV);
		final NodeView parent = nodeV.getParentView();
		int newIndex = 0;
		int oldIndex = -1;
		int wrondSideCount = 0;
		for(int i = start; i < end; i++){
			final Component component = parent.getComponent(i);
			if(!(component instanceof NodeView))
				continue;
			NodeView sibling = (NodeView)component;
			if(sibling.isLeft() == left 
					&& ! SummaryNode.isSummaryNode(sibling.getModel())
				&& getRefY(sibling) > nodeY)
				break;
			else{
				if(sibling != nodeV){
					newIndex++;
					if(sibling.isLeft() != left)
						wrondSideCount++;
					else
						wrondSideCount = 0;
				}
				else{
					oldIndex = i;
				}
			}
		}
		final int result = newIndex - wrondSideCount;
		if(result == oldIndex)
			return -1;
		return result;
    }
	private int getRefY(NodeView sibling) {
	    return sibling.getY() + sibling.getContent().getY();
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
