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
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.JScrollPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.DoubleClickTimer;
import org.freeplane.core.ui.IEditHandler.FirstAction;
import org.freeplane.core.ui.IMouseListener;
import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.Quantity;
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
	private Quantity<LengthUnits> originalHGap;
	private Quantity<LengthUnits> originalAssignedParentVGap;
	private Quantity<LengthUnits> minimalDistanceBetweenChildren;
	private Quantity<LengthUnits> originalShiftY;
	private static final String EDIT_ON_DOUBLE_CLICK = "edit_on_double_click";

	public MNodeMotionListener() {
	}

	Point getDragStartingPoint() {
		return dragStartingPoint;
	}

	/**
	 */
	private int getHGapChange(final Point dragNextPoint, final NodeModel node) {
		final Controller controller = Controller.getCurrentController();
		final MapView mapView = ((MapView) controller.getMapViewManager().getMapViewComponent());
		int hGapChange = (int) ((dragNextPoint.x - dragStartingPoint.x) / mapView.getZoom());
		if (node.isLeft()) {
			hGapChange = -hGapChange;
		}
		return hGapChange;
	}

	/**
	 */
	private int getNodeShiftYChange(final Point dragNextPoint, final NodeModel node) {
		final Controller controller = Controller.getCurrentController();
		final MapView mapView = ((MapView) controller.getMapViewManager().getMapViewComponent());
		final int shiftYChange = (int) ((dragNextPoint.y - dragStartingPoint.y) / mapView.getZoom());
		return shiftYChange;
	}

	/**
	 */
	private NodeView getNodeView(final MouseEvent e) {
		return ((MainView) e.getSource()).getNodeView();
	}

	/**
	 */
	private int getVGapChange(final Point dragNextPoint, final NodeModel node) {
		final Controller controller = Controller.getCurrentController();
		final MapView mapView = ((MapView) controller.getMapViewManager().getMapViewComponent());
		final int vGapChange = (int) ((dragNextPoint.y - dragStartingPoint.y) / mapView.getZoom());
		return vGapChange;
	}

	public boolean isDragActive() {
		return dragStartingPoint != null;
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		boolean shoudResetPosition = e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2
				&& doubleClickTimer.getDelay() > 0;
		if (shoudResetPosition) {
			final MainView mainView = (MainView) e.getComponent();
			if (mainView.getMouseArea().equals(MouseArea.MOTION)) {
				final Controller controller = Controller.getCurrentController();
				MLocationController locationController = (MLocationController) LocationController
				    .getController(controller.getModeController());
				if (e.getModifiersEx() == 0) {
					final NodeView nodeV = getNodeView(e);
					final NodeModel node = nodeV.getModel();
					locationController.moveNodePosition(node, LocationModel.DEFAULT_HGAP, LocationModel.DEFAULT_SHIFT_Y);
					return;
				}
				if (Compat.isCtrlEvent(e)) {
					final NodeView nodeV = getNodeView(e);
					NodeModel childDistanceContainer = nodeV.getParentView().getChildDistanceContainer().getModel();
					locationController.setMinimalDistanceBetweenChildren(childDistanceContainer, LocationModel.DEFAULT_VGAP);
					return;
				}
			}
			else {
				if (Compat.isPlainEvent(e) && !isInFoldingRegion(e)) {
					final MTextController textController = MTextController.getController();
					textController.getEventQueue().activate(e);
					textController.edit(FirstAction.EDIT_CURRENT, false);
				}
			}
		}
		super.mouseClicked(e);
	}

	@Override
    public void mouseMoved(final MouseEvent e) {
		if (isDragActive())
			return;
		final MainView v = (MainView) e.getSource();
		if (v.isInDragRegion(e.getPoint())) {
			v.setMouseArea(MouseArea.MOTION);
			v.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			return;
		}
		super.mouseMoved(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (!isDragActive())
			super.mouseExited(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		final MapView mapView = MapView.getMapView(e.getComponent());
		mapView.select();
		doubleClickTimer.cancel();
		setClickDelay();
		if (isInDragRegion(e)) {
			if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) == (InputEvent.BUTTON1_DOWN_MASK)) {
				nodeSelector.stopTimerForDelayedSelection();
				final NodeView nodeV = getNodeView(e);
				final Point point = e.getPoint();
				UITools.convertPointToAncestor(nodeV, point, JScrollPane.class);
				findGridPoint(point);
				final NodeModel node = nodeV.getModel();
				dragStartingPoint = point;
				originalAssignedParentVGap = LocationModel.getModel(node.getParentNode()).getVGap();
				NodeModel childDistanceContainer = nodeV.getParentView().getChildDistanceContainer().getModel();
				minimalDistanceBetweenChildren = mapView.getModeController().getExtension(LocationController.class).getMinimalDistanceBetweenChildren(childDistanceContainer);
				originalHGap = LocationModel.getModel(node).getHGap();
				originalShiftY = LocationModel.getModel(node).getShiftY();
			}
		}
		else
			super.mousePressed(e);
	}

	@Override
    public void mouseDragged(final MouseEvent e) {
		final NodeView nodeV = getNodeView(e);
		if (!isDragActive()) {
			if(! nodeV.isSelected())
				super.mouseDragged(e);
			return;
		}
		if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) == (InputEvent.BUTTON1_DOWN_MASK)) {
			final MainView mainView = (MainView) e.getSource();
			final MapView mapView = nodeV.getMap();
			final Point point = e.getPoint();
			UITools.convertPointToAncestor(nodeV, point, JScrollPane.class);
			findGridPoint(point);
			ModeController c = Controller.getCurrentController().getModeController();
			final Point dragNextPoint = point;
			boolean shouldMoveSingleNode = !Compat.isCtrlEvent(e);
			if (shouldMoveSingleNode) {
				final NodeModel node = nodeV.getModel();
				final LocationModel locationModel = LocationModel.createLocationModel(node);
				final int hGapChange = getHGapChange(dragNextPoint, node);
				if(hGapChange != 0){
					locationModel.setHGap(originalHGap.add(hGapChange, LengthUnits.px));
				}
				final int shiftYChange = getNodeShiftYChange(dragNextPoint, node);
				if(shiftYChange != 0){
					locationModel.setShiftY(originalShiftY.add(shiftYChange, LengthUnits.px));
				}
				if(hGapChange != 0 || shiftYChange != 0)
					c.getMapController().nodeRefresh(node);
				else
					return;
			}
			else {
				final NodeModel childDistanceContainer = nodeV.getParentView().getChildDistanceContainer().getModel();
				final int vGapChange = getVGapChange(dragNextPoint, childDistanceContainer);
				int newVGap = Math.max(0, minimalDistanceBetweenChildren.toBaseUnitsRounded() - vGapChange);
				LocationModel locationModel = LocationModel.createLocationModel(childDistanceContainer);
				if(locationModel.getVGap().toBaseUnitsRounded() == newVGap)
					return;
				locationModel.setVGap(new Quantity<LengthUnits>(newVGap, LengthUnits.px).in(LengthUnits.pt));
				final MapController mapController = c.getMapController();
				mapController.nodeRefresh(childDistanceContainer);
				mapController.nodeRefresh(nodeV.getModel());
			}
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					final Rectangle r = mainView.getBounds();
					UITools.convertRectangleToAncestor(mainView.getParent(), r, mapView);
					final boolean isEventPointVisible = mapView.getVisibleRect().contains(r);
					if (!isEventPointVisible) {
						mapView.scrollRectToVisible(r);
					}
				}
			});
		}
	}

	private void findGridPoint(Point point) {
		final int gridSize = ResourceController.getResourceController().getLengthProperty("grid_size");
		if (gridSize <= 2) {
			return;
		}
		point.x -= point.x % gridSize;
		point.y -= point.y % gridSize;
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		final MainView v = (MainView) e.getSource();
		if (!v.contains(e.getX(), e.getY())) {
			v.setMouseArea(MouseArea.OUT);
		}
		if (!isDragActive()) {
			super.mouseReleased(e);
			return;
		}
		final NodeView nodeV = getNodeView(e);
		final NodeModel node = nodeV.getModel();
		final ModeController modeController = nodeV.getMap().getModeController();
		final Controller controller = modeController.getController();
		MLocationController locationController = (MLocationController) LocationController.getController(controller
				.getModeController());
		final NodeView parentView = nodeV.getParentView();
		final NodeView childDistanceContainerView = parentView.getChildDistanceContainer();
		NodeModel childDistanceContainer = childDistanceContainerView.getModel();
		final Quantity<LengthUnits> parentVGap = locationController.getMinimalDistanceBetweenChildren(childDistanceContainer);
		Quantity<LengthUnits> hgap = LocationModel.getModel(node).getHGap();
		final Quantity<LengthUnits> shiftY = LocationModel.getModel(node).getShiftY();
		adjustNodeIndices(nodeV);
		resetPositions(node);
		final Quantity<LengthUnits> hGap = hgap;
		locationController.moveNodePosition(node, hGap, shiftY);
		locationController.setMinimalDistanceBetweenChildren(childDistanceContainer, parentVGap);
		stopDrag();
	}

	private void adjustNodeIndices(final NodeView nodeV) {
		NodeModel[] selectedsBackup = null;
		final NodeModel node = nodeV.getModel();
		if (FreeNode.isFreeNode(node)) {
			selectedsBackup = adjustNodeIndexBackupSelection(nodeV, selectedsBackup);
		}
		else {
			final MapView map = nodeV.getMap();
			final NodeModel[] siblingNodes = node.getParentNode().getChildren().toArray(new NodeModel[] {});
			for (NodeModel sibling : siblingNodes) {
				if (FreeNode.isFreeNode(sibling)) {
					final NodeView siblingV = map.getNodeView(sibling);
					selectedsBackup = adjustNodeIndexBackupSelection(siblingV, selectedsBackup);
				}
			}
		}
		if (selectedsBackup != null) {
			final ModeController modeController = nodeV.getMap().getModeController();
			final Controller controller = modeController.getController();
			controller.getSelection().replaceSelection(selectedsBackup);
		}
	}

	private NodeModel[] adjustNodeIndexBackupSelection(final NodeView nodeV, NodeModel[] selectedsBackup) {
		final NodeModel node = nodeV.getModel();
		final int newIndex = calculateNewFreeNodeIndex(nodeV);
		if (newIndex != -1) {
			final ModeController modeController = nodeV.getMap().getModeController();
			MMapController mapController = (MMapController) modeController.getMapController();
			if (selectedsBackup == null) {
				final Collection<NodeModel> selecteds = mapController.getSelectedNodes();
				selectedsBackup = selecteds.toArray(new NodeModel[selecteds.size()]);
			}
			mapController.moveNode(node, newIndex);
		}
		return selectedsBackup;
	}

	public int getRefX(final NodeView node) {
		return node.getContent().getX() + node.getContent().getWidth() / 2;
	}

	private int calculateNewFreeNodeIndex(final NodeView nodeV) {
		final NodeModel node = nodeV.getModel();
		if (SummaryNode.isHidden(node))
			return -1;
		final boolean left = nodeV.isLeft();
		final int nodeY = getRefY(nodeV);
		final NodeView parent = nodeV.getParentView();
		int newIndex = 0;
		int oldIndex = -1;
		int wrondSideCount = 0;
		int hiddenNodeCount = 0;
		final int childCount = node.getParentNode().getChildCount();
		for (int i = 0; i < childCount; i++) {
			final Component component = parent.getComponent(i);
			if (!(component instanceof NodeView))
				continue;
			NodeView siblingV = (NodeView) component;
			final NodeModel sibling = siblingV.getModel();
			if (siblingV.isLeft() == left && !SummaryNode.isHidden(sibling) && getRefY(siblingV) > nodeY)
				break;
			else {
				if (siblingV != nodeV) {
					newIndex++;
					if (siblingV.isLeft() != left)
						wrondSideCount++;
					else {
						wrondSideCount = 0;
						if(oldIndex >= 0 && SummaryNode.isHidden(sibling))
							hiddenNodeCount++;
						else
							hiddenNodeCount = 0;
					}
				}
				else {
					oldIndex = i;
				}
			}
		}
		final int result = newIndex - wrondSideCount - hiddenNodeCount;
		if (result == oldIndex)
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
		locationModel.setVGap(originalAssignedParentVGap);
		LocationModel.getModel(node).setHGap(originalHGap);
		LocationModel.getModel(node).setShiftY(originalShiftY);
	}

	private void resetDragStartingPoint() {
		dragStartingPoint = null;
		minimalDistanceBetweenChildren = originalAssignedParentVGap = LocationModel.DEFAULT_VGAP;
		originalHGap = LocationModel.DEFAULT_HGAP;
		originalShiftY = LocationModel.DEFAULT_SHIFT_Y;
	}

	private void stopDrag() {
		resetDragStartingPoint();
	}

	private void setClickDelay() {
	    if (ResourceController.getResourceController().getBooleanProperty(EDIT_ON_DOUBLE_CLICK))
	        doubleClickTimer.setDelay(DoubleClickTimer.MAX_TIME_BETWEEN_CLICKS);
        else {
	    	doubleClickTimer.setDelay(0);
	    }
    }
}
