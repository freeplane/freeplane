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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JScrollPane;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.DoubleClickTimer;
import org.freeplane.core.ui.IEditHandler.FirstAction;
import org.freeplane.core.ui.IMouseListener;
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
    private Quantity<LengthUnit> originalHGap;
    private Quantity<LengthUnit> originalAssignedBaseHGap;
    private List<NodeModel> selection;
    private List<NodeModel> parentSelection;
    private List<Quantity<LengthUnit>> originalHGaps;
    private List<Quantity<LengthUnit>> originalAssignedBaseHGaps;
    private List<Quantity<LengthUnit>> originalAssignedVGaps;
	private Quantity<LengthUnit> originalAssignedParentVGap;
    private Quantity<LengthUnit> effectiveParentVGap;
    private Quantity<LengthUnit> effectiveBaseHGap;
	private Quantity<LengthUnit> originalShiftY;
    private NodeView draggedNodeView;
	private static final String EDIT_ON_DOUBLE_CLICK = "edit_on_double_click";

	public MNodeMotionListener() {
	}

	Point getDragStartingPoint() {
		return dragStartingPoint;
	}

	/**
	 */
	private int getHGapChange(final Point dragNextPoint, boolean usesHorizontalLayout) {
		final Controller controller = Controller.getCurrentController();
		final MapView mapView = ((MapView) controller.getMapViewManager().getMapViewComponent());
		int distance = usesHorizontalLayout ? dragNextPoint.y - dragStartingPoint.y : dragNextPoint.x - dragStartingPoint.x;
        int hGapChange = (int) (distance / mapView.getZoom());
		if (getNode().isTopOrLeft(mapView.getRoot().getModel())) {
			hGapChange = -hGapChange;
		}
		return hGapChange;
	}

	/**
	 */
	private int getNodeShiftYChange(final Point dragNextPoint, boolean usesHorizontalLayout) {
		final Controller controller = Controller.getCurrentController();
		final MapView mapView = ((MapView) controller.getMapViewManager().getMapViewComponent());
        int distance = usesHorizontalLayout ? dragNextPoint.x - dragStartingPoint.x : dragNextPoint.y - dragStartingPoint.y;
		final int shiftYChange = (int) (distance / mapView.getZoom());
		return shiftYChange;
	}


	public boolean isDragActive() {
		return dragStartingPoint != null;
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		boolean shouldEditOrResetPosition = e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2
				&& doubleClickTimer.getDelay() > 0;
		if (shouldEditOrResetPosition) {
			final MainView mainView = (MainView) e.getComponent();
			if (mainView.getMouseArea().equals(MouseArea.MOTION)) {
				final Controller controller = Controller.getCurrentController();
				MLocationController locationController = (MLocationController) LocationController
				    .getController(controller.getModeController());
				if (e.getModifiersEx() == 0) {
					final NodeView nodeV = mainView.getNodeView();
					final NodeModel node = nodeV.getModel();
					Set<NodeModel> currentSelection = controller.getSelection().getSelection();
					if(currentSelection.size() > 1) {
					    currentSelection.forEach(n ->
					        locationController.moveNodePosition(n, LocationModel.DEFAULT_HGAP, LocationModel.getModel(n).getShiftY()));
					}
					locationController.moveNodePosition(node, LocationModel.DEFAULT_HGAP, LocationModel.DEFAULT_SHIFT_Y);
					return;
				}
				if (Compat.isCtrlEvent(e)) {
					final NodeView nodeV = mainView.getNodeView();
					Set<NodeModel> currentSelection = controller.getSelection().getSelection();
                    if(currentSelection.size() > 1) {
                        currentSelection.forEach(n ->
                            {
                                NodeModel parentNode = n.getParentNode();
                                locationController.setBaseHGapToChildren(parentNode, LocationModel.DEFAULT_BASE_HGAP);
                                locationController.setCommonVGapBetweenChildren(parentNode, LocationModel.DEFAULT_VGAP);
                            });
                    }
                    else {
                        NodeModel parentNode = nodeV.getParentView().getModel();
                        locationController.setBaseHGapToChildren(parentNode, LocationModel.DEFAULT_BASE_HGAP);
                        locationController.setCommonVGapBetweenChildren(parentNode, LocationModel.DEFAULT_VGAP);
                    }
					return;
				}
			}
			else {
				if (Compat.isPlainEvent(e) && !isInFoldingRegion(e) && ! mainView.isInDragRegion(e.getPoint())) {
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
		final MainView mainView = (MainView) e.getSource();
		final NodeView nodeV = mainView.getNodeView();
		MapView mapView = nodeV.getMap();
		ModeController modeController = mapView.getModeController();
		if(modeController.canEdit(mapView.getModel())) {
			if (mainView.isInDragRegion(e.getPoint())) {
				mainView.setMouseArea(MouseArea.MOTION);
				mainView.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				return;
			}
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
			    NodeView nodeView = ((MainView) e.getSource()).getNodeView();
				final NodeModel node = nodeView.getModel();
				ModeController modeController = mapView.getModeController();
				if(modeController.canEdit(node.getMap())) {
					nodeSelector.stopTimerForDelayedSelection();
					final Point point = e.getPoint();
					UITools.convertPointToAncestor(nodeView, point, JScrollPane.class);
					findGridPoint(point);
					draggedNodeView = nodeView;
					dragStartingPoint = point;
					{
					    LocationModel parentLocationMode = LocationModel.getModel(node.getParentNode());
					    originalAssignedParentVGap = parentLocationMode.getVGap();
					    originalAssignedBaseHGap = parentLocationMode.getBaseHGap();
					}
					NodeModel childDistanceContainer = draggedNodeView.getParentView().getModel();
					LocationController extension = modeController.getExtension(LocationController.class);
                    effectiveParentVGap = extension.getCommonVGapBetweenChildren(childDistanceContainer);
                    effectiveBaseHGap = extension.getBaseHGapToChildren(childDistanceContainer);
					originalHGap = LocationModel.getModel(node).getHGap();
					originalShiftY = LocationModel.getModel(node).getShiftY();
					Set<NodeModel> selection = modeController.getController().getSelection().getSelection();
					if(selection.size() > 1) {
					    this.selection = new ArrayList<>(selection);
					    originalAssignedBaseHGaps = new ArrayList<>();
					    originalAssignedVGaps = new ArrayList<>();
					    originalHGaps = new ArrayList<>(selection.size());
					    this.selection.forEach(n -> originalHGaps.add(LocationModel.getModel(n).getHGap()));
					    Set<NodeModel> parentSelection = new HashSet<>();
					    this.selection.forEach(n -> {
                            NodeModel parentNode = n.getParentNode();
                            if (parentSelection.add(parentNode)) {
                                LocationModel parentLocationModel = LocationModel.getModel(parentNode);
                                originalAssignedBaseHGaps.add(parentLocationModel.getBaseHGap());
                                originalAssignedVGaps.add(parentLocationModel.getVGap());
                            }

                        });
					    this.parentSelection = new ArrayList<>(parentSelection);
					}
				}
			}
		}
		else
			super.mousePressed(e);
	}

	@Override
    public void mouseDragged(final MouseEvent e) {
		if (!isDragActive()) {
			if(! draggedNodeView.isSelected())
				super.mouseDragged(e);
			return;
		}
		if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) == (InputEvent.BUTTON1_DOWN_MASK)) {
			final MainView mainView = (MainView) e.getSource();
			final MapView mapView = draggedNodeView.getMap();
			final Point point = e.getPoint();
			UITools.convertPointToAncestor(draggedNodeView, point, JScrollPane.class);
			findGridPoint(point);
			Controller controller = Controller.getCurrentController();
            ModeController modeController = controller.getModeController();
			final Point dragNextPoint = point;
			boolean changesDistanceBetweenChildren = Compat.isCtrlEvent(e);
			boolean movesSingleNodeInBothDirections = Compat.isShiftEvent(e);
			boolean usesHorizontalLayout = draggedNodeView.getAncestorWithVisibleContent().usesHorizontalLayout();
			final NodeModel node = getNode();
			final int hGapChange = getHGapChange(dragNextPoint, usesHorizontalLayout);
			final int shiftYChange = getNodeShiftYChange(dragNextPoint, usesHorizontalLayout);
			if (! changesDistanceBetweenChildren) {
			    final LocationModel locationModel = LocationModel.createLocationModel(node);
			    boolean isHgapChanged = originalHGap.toBaseUnitsRounded() + hGapChange != locationModel.getHGap().toBaseUnitsRounded();
			    boolean isShiftYChanged = originalShiftY.toBaseUnitsRounded() + shiftYChange != locationModel.getShiftY().toBaseUnitsRounded();
	             if(! isHgapChanged && !isShiftYChanged)
	                 return;

                if(movesSingleNodeInBothDirections || Math.abs(hGapChange) >= Math.abs(shiftYChange)) {
			        Quantity<LengthUnit> newHGap = originalHGap.add(hGapChange, LengthUnit.px);
                    setCurrentHGap(newHGap);
                    if(! movesSingleNodeInBothDirections)
                        locationModel.setShiftY(originalShiftY);
                }
                if(movesSingleNodeInBothDirections || Math.abs(hGapChange) < Math.abs(shiftYChange)){
					locationModel.setShiftY(originalShiftY.add(shiftYChange, LengthUnit.px));
					if(! movesSingleNodeInBothDirections && ! locationModel.getHGap().equals(originalHGap)) {
					    resetHGaps();
					}
				}
                final MapController mapController = modeController.getMapController();
                if(movesMultipleNodes()) {
                    selection.forEach(mapController::nodeRefresh);

                }
                else
                    mapController.nodeRefresh(node);
			}
			else {
				final NodeModel parentNode = draggedNodeView.getParentView().getModel();
				int newVGap = Math.max(0, effectiveParentVGap.toBaseUnitsRounded() + (usesHorizontalLayout ?  shiftYChange : -shiftYChange));
                final LocationModel locationModel = LocationModel.createLocationModel(parentNode);
                boolean isHgapChanged = originalHGap.toBaseUnitsRounded() + hGapChange != locationModel.getBaseHGap().toBaseUnitsRounded();
				boolean isVgapChanged = locationModel.getVGap().toBaseUnitsRounded() != newVGap;
                if(! isVgapChanged && ! isHgapChanged)
					return;

                if(movesSingleNodeInBothDirections || Math.abs(hGapChange) >= Math.abs(shiftYChange)) {
                    Quantity<LengthUnit> newHGap = effectiveBaseHGap.add(hGapChange, LengthUnit.px);
                    setCurrentBaseHGap(newHGap);
                    if(! movesSingleNodeInBothDirections)
                        resetVGaps();
                }
                if(movesSingleNodeInBothDirections || Math.abs(hGapChange) < Math.abs(shiftYChange)){
                    setCurrentVGap(new Quantity<LengthUnit>(newVGap, LengthUnit.px).in(LengthUnit.pt));
                    if(! movesSingleNodeInBothDirections) {
                        resetBaseHGaps();
                    }
                }
                final MapController mapController = modeController.getMapController();
                if(movesMultipleNodes()) {
                    parentSelection.forEach(mapController::nodeRefresh);

                }
                else
                    mapController.nodeRefresh(node.getParentNode());

			}
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
				    if(! mainView.isShowing())
				        return;
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

    private void setCurrentHGap(Quantity<LengthUnit> newHGap) {
        if (movesMultipleNodes()) {
            selection.forEach(n ->
                LocationModel.createLocationModel(n).setHGap(newHGap));
        } else {
            LocationModel.createLocationModel(getNode()).setHGap(newHGap);
        }
    }

    private void setCurrentVGap(Quantity<LengthUnit> newVGap) {
        if (movesMultipleNodes()) {
            parentSelection.forEach(n ->
                LocationModel.createLocationModel(n).setVGap(newVGap));
        } else {
            LocationModel.createLocationModel(getNode().getParentNode()).setVGap(newVGap);
        }
    }

    private void setCurrentBaseHGap(Quantity<LengthUnit> newHGap) {
        if (movesMultipleNodes()) {
            parentSelection.forEach(n ->
                LocationModel.createLocationModel(n).setBaseHGap(newHGap));
        } else {
            LocationModel.createLocationModel(getNode().getParentNode()).setBaseHGap(newHGap);
        }
    }

//    locationModel.setVGap(new Quantity<LengthUnit>(newVGap, LengthUnit.px).in(LengthUnit.pt));

    private boolean movesMultipleNodes() {
        return selection != null;
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
		final NodeModel node = getNode();
		final ModeController modeController = draggedNodeView.getMap().getModeController();
		final Controller controller = modeController.getController();
		MLocationController locationController = (MLocationController) LocationController.getController(controller
				.getModeController());
		final NodeView parentView = draggedNodeView.getParentView();
		NodeModel parentNode = parentView.getModel();
        final Quantity<LengthUnit> parentVGap = locationController.getCommonVGapBetweenChildren(parentNode);
        final Quantity<LengthUnit> baseHGap = locationController.getBaseHGapToChildren(parentNode);
		Quantity<LengthUnit> hgap = LocationModel.getModel(node).getHGap();
		final Quantity<LengthUnit> shiftY = LocationModel.getModel(node).getShiftY();
		adjustNodeIndices();
		resetPositions();
		locationController.moveNodePosition(node, hgap, shiftY);
		locationController.setBaseHGapToChildren(parentNode, baseHGap);
        locationController.setCommonVGapBetweenChildren(parentNode, parentVGap);
        if(movesMultipleNodes()) {
            selection.forEach(n ->
            {
                if(n != node)
                    locationController.moveNodePosition(n, hgap, LocationModel.getModel(n).getShiftY());
            });
            parentSelection.forEach(n ->
            {
                if(n != node) {
                    locationController.setBaseHGapToChildren(n, baseHGap);
                    locationController.setCommonVGapBetweenChildren(n, parentVGap);
                }
            });
        }
		stopDrag();
	}

    private NodeModel getNode() {
        return draggedNodeView.getModel();
    }

	private void adjustNodeIndices() {
	    NodeModel[] selectedsBackup = null;
		final NodeModel node = getNode();
		if (FreeNode.isFreeNode(node)) {
			selectedsBackup = adjustNodeIndexBackupSelection(draggedNodeView, selectedsBackup);
		}
		else {
			final MapView map = draggedNodeView.getMap();
			final NodeModel[] siblingNodes = node.getParentNode().getChildren().toArray(new NodeModel[] {});
			for (NodeModel sibling : siblingNodes) {
				if (FreeNode.isFreeNode(sibling)) {
					final NodeView siblingV = map.getNodeView(sibling);
					selectedsBackup = adjustNodeIndexBackupSelection(siblingV, selectedsBackup);
				}
			}
		}
		if (selectedsBackup != null) {
			final ModeController modeController = draggedNodeView.getMap().getModeController();
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
		final boolean left = nodeV.isTopOrLeft();
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
			if (siblingV.isTopOrLeft() == left && !SummaryNode.isHidden(sibling) && getRefY(siblingV) > nodeY)
				break;
			else {
				if (siblingV != nodeV) {
					newIndex++;
					if (siblingV.isTopOrLeft() != left)
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
	private void resetPositions() {
        resetCommonGaps();
        NodeModel node = getNode();
		LocationModel.getModel(node).setShiftY(originalShiftY);
		resetHGaps();
	}

    private void resetCommonGaps() {
        resetBaseHGaps();
        resetVGaps();
    }

    private void resetBaseHGaps() {
        NodeModel node = getNode();
        if (movesMultipleNodes()) {
            for(int i = 0; i < parentSelection.size(); i++) {
                LocationModel locationModel = LocationModel.getModel(parentSelection.get(i));
                locationModel.setBaseHGap(originalAssignedBaseHGaps.get(i));
            }
        } else {
            final LocationModel locationModel = LocationModel.getModel(node.getParentNode());
            locationModel.setBaseHGap(originalAssignedBaseHGap);
        }
    }

    private void resetVGaps() {
        NodeModel node = getNode();
        if (movesMultipleNodes()) {
            for(int i = 0; i < parentSelection.size(); i++) {
                LocationModel locationModel = LocationModel.getModel(parentSelection.get(i));
                locationModel.setVGap(originalAssignedVGaps.get(i));
            }
        } else {
            final LocationModel locationModel = LocationModel.getModel(node.getParentNode());
            locationModel.setVGap(originalAssignedParentVGap);
        }
    }

    private void resetHGaps() {
        if (movesMultipleNodes()) {
            for(int i = 0; i < selection.size(); i++) {
                LocationModel.getModel(selection.get(i)).setHGap(originalHGaps.get(i));
            }
        }
		else
		    LocationModel.getModel(getNode()).setHGap(originalHGap);
    }

	private void resetDragStartingPoint() {
		dragStartingPoint = null;
		effectiveParentVGap = originalAssignedParentVGap = LocationModel.DEFAULT_VGAP;
		effectiveBaseHGap = originalAssignedBaseHGap = LocationModel.DEFAULT_BASE_HGAP;
		originalHGap = LocationModel.DEFAULT_HGAP;
		originalShiftY = LocationModel.DEFAULT_SHIFT_Y;
		originalHGaps = originalAssignedBaseHGaps = originalAssignedVGaps = null;
		selection = parentSelection = null;

		draggedNodeView = null;
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
