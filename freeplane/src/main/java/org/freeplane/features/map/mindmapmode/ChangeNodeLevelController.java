/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package org.freeplane.features.map.mindmapmode;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.freeplane.api.ChildrenSides;
import org.freeplane.api.LayoutOrientation;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.layout.LayoutController;
import org.freeplane.features.map.FreeNode;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeModel.Side;
import org.freeplane.features.map.SummaryNode;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.MapViewLayout;
import org.freeplane.features.ui.IMapViewManager;

/**
 * @author foltin
 */
public class ChangeNodeLevelController implements IExtension {
	class ChangeNodeLevelLeftsAction extends AFreeplaneAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public ChangeNodeLevelLeftsAction() {
			super("ChangeNodeLevelLeftsAction");
		}

		@Override
        public void actionPerformed(final ActionEvent e) {
            Controller controller = Controller.getCurrentController();
            IMapSelection selection = controller.getSelection();
            NodeModel selectedNode = selection.getSelected();
            NodeModel selectionRoot = selection.getSelectionRoot();
            if(selectedNode == selectionRoot)
                return;
            LayoutController layoutController = LayoutController.getController();
            boolean selectionUsesHorizontalLayout = layoutController.getEffectiveLayoutOrientation(selectedNode.getParentNode()) == LayoutOrientation.LEFT_TO_RIGHT;
            if(selectionUsesHorizontalLayout) {
                MMapController mapController = (MMapController) controller.getModeController().getMapController();
                mapController.moveNodesInGivenDirection(selectionRoot, selectedNode, selection.getOrderedSelection(), -1);
            }
            else {
                ChangeNodeLevelController levelController = controller.getModeController().getExtension(ChangeNodeLevelController.class);
                levelController.changeNodeLevelLefts(selectionRoot, selectedNode);
            }
		}
	}

	static class ChangeNodeLevelRightsAction extends AFreeplaneAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public ChangeNodeLevelRightsAction() {
			super("ChangeNodeLevelRightsAction");
		}

		@Override
        public void actionPerformed(final ActionEvent e) {
			Controller controller = Controller.getCurrentController();
			IMapSelection selection = controller.getSelection();
			NodeModel selectedNode = selection.getSelected();
			NodeModel selectionRoot = selection.getSelectionRoot();
			if(selectedNode == selectionRoot)
			    return;
			LayoutController layoutController = LayoutController.getController();
            boolean selectionUsesHorizontalLayout = layoutController.getEffectiveLayoutOrientation(selectedNode.getParentNode()) == LayoutOrientation.LEFT_TO_RIGHT;
            if(selectionUsesHorizontalLayout) {
                MMapController mapController = (MMapController) controller.getModeController().getMapController();
                mapController.moveNodesInGivenDirection(selectionRoot, selectedNode, selection.getOrderedSelection(), 1);
            }
            else {
                ChangeNodeLevelController levelController = controller.getModeController().getExtension(ChangeNodeLevelController.class);
                levelController.changeNodeLevelRights(selectionRoot, selectedNode);
            }
		}

	}

// // 	final private Controller controller;;

	public ChangeNodeLevelController(MModeController modeController) {
//		this.controller = controller;
	    modeController.addExtension(ChangeNodeLevelController.class, this);
		modeController.addAction(new ChangeNodeLevelLeftsAction());
		modeController.addAction(new ChangeNodeLevelRightsAction());
	}

	private boolean checkSelection() {
		Controller controller = Controller.getCurrentController();
		IMapSelection selection = controller.getSelection();
		NodeModel selectedNode = selection.getSelected();
		final NodeModel selectedParent = selectedNode.getParentNode();
		if (selectedParent == null) {
			UITools.errorMessage(TextUtils.getText("cannot_add_parent_to_root"));
			return false;
		}
		final Collection<NodeModel> selectedNodes = selection.getSelection();
		for (final NodeModel node : selectedNodes) {
			if (node.getParentNode() != selectedParent) {
				UITools.errorMessage(TextUtils.getText("cannot_add_parent_diff_parents"));
				return false;
			}
		}
		return true;
	}

	private void moveDownwards(final NodeModel selectionRoot, final NodeModel selectedNode) {
		if (!checkSelection()) {
			return;
		}
		final NodeModel selectedParent = selectedNode.getParentNode();
		final List<NodeModel> selectedNodes = Controller.getCurrentController().getSelection().getSortedSelection(true);
		final MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
		final int ownPosition = selectedParent.getIndex(selectedNode);
		NodeModel directSibling = null;
		for (int i = ownPosition - 1; i >= 0; --i) {
			final NodeModel targetCandidate = selectedParent.getChildAt(i);
			if (canMoveTo(selectionRoot, selectedNode, selectedNodes, targetCandidate)) {
				directSibling = targetCandidate;
				break;
			}
		}
		if (directSibling == null) {
			for (int i = ownPosition + 1; i < selectedParent.getChildCount(); ++i) {
				final NodeModel targetCandidate = selectedParent.getChildAt(i);
				if (canMoveTo(selectionRoot, selectedNode, selectedNodes, targetCandidate)) {
					directSibling = targetCandidate;
					break;
				}
			}
		}
		if (directSibling != null) {
			for (final NodeModel node : selectedNodes) {
				(Controller.getCurrentModeController().getExtension(FreeNode.class)).undoableDeactivateHook(node);
			}
			mapController.moveNodes(selectedNodes, directSibling, directSibling.getChildCount());
			Controller.getCurrentModeController().getMapController().selectMultipleNodes(selectedNode, selectedNodes);
		}
	}

	private boolean canMoveTo(final NodeModel selectionRoot, final NodeModel selectedNode, final List<NodeModel> selectedNodes,
			final NodeModel targetCandidate) {
		return !selectedNodes.contains(targetCandidate) && selectedNode.isTopOrLeft(selectionRoot) == targetCandidate.isTopOrLeft(selectionRoot)
				&& (targetCandidate.hasChildren() || ! targetCandidate.isHiddenSummary());
	}

	private void moveUpwards(final NodeModel selectionRoot, NodeModel selectedNode) {
		if (!checkSelection()) {
			return;
		}
		final MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
		NodeModel selectedParent = selectedNode.getParentNode();
		final List<NodeModel> selectedNodes = Controller.getCurrentController().getSelection().getSortedSelection(true);
		if (selectedParent == selectionRoot ||
		        LayoutController.getController().getEffectiveChildNodesLayout(selectedParent).childrenSides() == ChildrenSides.BOTH_SIDES) {
			final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
			final Component mapViewComponent = mapViewManager.getMapViewComponent();
			if (!mapViewManager.isLeftTreeSupported(mapViewComponent)) {
				return;
			}
			Side newSide = selectedNode.isTopOrLeft(selectionRoot) ? Side.BOTTOM_OR_RIGHT : Side.TOP_OR_LEFT;
			mapController.setSide(selectedNodes, newSide);
		}
		else {
			final NodeModel grandParent = selectedParent.getParentNode();
			final NodeModel childNode = selectedParent;
			int position = grandParent.getIndex(childNode) + 1;
			selectedParent = grandParent;
			final MapStyleModel mapStyleModel = MapStyleModel.getExtension(selectedParent.getMap());
			MapViewLayout layoutType = mapStyleModel.getMapViewLayout();
			List<List<NodeModel>> movedChildren = layoutType == MapViewLayout.OUTLINE ? findMovedChildren(selectedNode.getParentNode(), selectedNodes) : Collections.emptyList();
			for (final NodeModel node : selectedNodes)
				(Controller.getCurrentModeController().getExtension(FreeNode.class)).undoableDeactivateHook(node);
			mapController.moveNodes(selectedNodes, selectedParent, position);
			if(layoutType == MapViewLayout.OUTLINE) {
				for(int i = 0; i < selectedNodes.size(); i++) {
					mapController.moveNodes(movedChildren.get(i), selectedNodes.get(i), 0);
				}
			}
		}
		mapController.selectMultipleNodes(selectedNode, selectedNodes);
	}

    private List<List<NodeModel>> findMovedChildren(NodeModel parent, List<NodeModel> movedNodes) {
        List<List<NodeModel>> movedChildren = new ArrayList<>(movedNodes.size());
        int movedNodeCounter = 0;
        List<NodeModel> children = parent.getChildren();
        for(NodeModel node : children) {
            if(movedNodeCounter < movedNodes.size() && node == movedNodes.get(movedNodeCounter)) {
                movedNodeCounter++;
                movedChildren.add(new ArrayList<>());
            }
            else if(movedNodeCounter > 0) {
                List<NodeModel> list = movedChildren.get(movedNodeCounter - 1);
                if(list.size() > 0 || ! SummaryNode.isSummaryNode(node))
                    list.add(node);
            }
        }
        return movedChildren;
    }

    void changeNodeLevelLefts(NodeModel selectionRoot, NodeModel selectedNode) {
        final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
        final Component mapViewComponent = mapViewManager.getMapViewComponent();
        if (mapViewManager.isLeftTreeSupported(mapViewComponent) && selectedNode.isTopOrLeft(selectionRoot)) {
        	moveDownwards(selectionRoot, selectedNode);
        }
        else {
        	moveUpwards(selectionRoot, selectedNode);
        }
    }

    void changeNodeLevelRights(NodeModel selectionRoot, NodeModel selectedNode) {
        final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
        final Component mapViewComponent = mapViewManager.getMapViewComponent();
        if (mapViewManager.isLeftTreeSupported(mapViewComponent) && selectedNode.isTopOrLeft(selectionRoot)) {
            moveUpwards(selectionRoot, selectedNode);
        }
        else {
            moveDownwards(selectionRoot, selectedNode);
        }
    }
}
