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
import java.util.Collection;
import java.util.List;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.FreeNode;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.ui.IMapViewManager;

/**
 * @author foltin
 */
public class ChangeNodeLevelController {
	private class ChangeNodeLevelLeftsAction extends AFreeplaneAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ChangeNodeLevelLeftsAction() {
			super("ChangeNodeLevelLeftsAction");
		}

		public void actionPerformed(final ActionEvent e) {
			final ModeController modeController = Controller.getCurrentModeController();
			final NodeModel selectedNode = modeController.getMapController().getSelectedNode();
			final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
			final Component mapViewComponent = mapViewManager.getMapViewComponent();
			if (mapViewManager.isLeftTreeSupported(mapViewComponent) && selectedNode.isLeft()) {
				moveDownwards(selectedNode);
			}
			else {
				moveUpwards(selectedNode);
			}
		}
	}

	private class ChangeNodeLevelRightsAction extends AFreeplaneAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ChangeNodeLevelRightsAction() {
			super("ChangeNodeLevelRightsAction");
		}

		public void actionPerformed(final ActionEvent e) {
			final ModeController modeController = Controller.getCurrentModeController();
			final NodeModel selectedNode = modeController.getMapController().getSelectedNode();
			final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
			final Component mapViewComponent = mapViewManager.getMapViewComponent();
			if (mapViewManager.isLeftTreeSupported(mapViewComponent) && selectedNode.isLeft()) {
				moveUpwards(selectedNode);
			}
			else {
				moveDownwards(selectedNode);
			}
		}
	};

// // 	final private Controller controller;;

	public ChangeNodeLevelController(MModeController modeController) {
//		this.controller = controller;
		modeController.addAction(new ChangeNodeLevelLeftsAction());
		modeController.addAction(new ChangeNodeLevelRightsAction());
	}

	private boolean checkSelection() {
		final ModeController currentModeController = Controller.getCurrentModeController();
		final MapController mapController = currentModeController.getMapController();
		final NodeModel selectedNode = mapController.getSelectedNode();
		final NodeModel selectedParent = selectedNode.getParentNode();
		if (selectedParent == null) {
			UITools.errorMessage(TextUtils.getText("cannot_add_parent_to_root"));
			return false;
		}
		final Collection<NodeModel> selectedNodes = mapController.getSelectedNodes();
		for (final NodeModel node : selectedNodes) {
			if (node.getParentNode() != selectedParent) {
				UITools.errorMessage(TextUtils.getText("cannot_add_parent_diff_parents"));
				return false;
			}
		}
		return true;
	}

	private void moveDownwards( final NodeModel selectedNode) {
		if (!checkSelection()) {
			return;
		}
		final NodeModel selectedParent = selectedNode.getParentNode();
		final List<NodeModel> selectedNodes = Controller.getCurrentController().getSelection().getSortedSelection(true);
		final MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
		final int ownPosition = selectedParent.getIndex(selectedNode);
		NodeModel directSibling = null;
		for (int i = ownPosition - 1; i >= 0; --i) {
			final NodeModel targetCandidate = (NodeModel) selectedParent.getChildAt(i);
			if (canMoveTo(selectedNode, selectedNodes, targetCandidate)) {
				directSibling = targetCandidate;
				break;
			}
		}
		if (directSibling == null) {
			for (int i = ownPosition + 1; i < selectedParent.getChildCount(); ++i) {
				final NodeModel targetCandidate = (NodeModel) selectedParent.getChildAt(i);
				if (canMoveTo(selectedNode, selectedNodes, targetCandidate)) {
					directSibling = targetCandidate;
					break;
				}
			}
		}
		if (directSibling != null) {
			for (final NodeModel node : selectedNodes) {
				((FreeNode)Controller.getCurrentModeController().getExtension(FreeNode.class)).undoableDeactivateHook(node);
			}
			mapController.moveNodes(selectedNodes, directSibling, directSibling.getChildCount());
		}
	}

	private boolean canMoveTo(final NodeModel selectedNode, final List<NodeModel> selectedNodes,
			final NodeModel targetCandidate) {
		return !selectedNodes.contains(targetCandidate) && selectedNode.isLeft() == targetCandidate.isLeft() 
				&& (targetCandidate.hasChildren() || ! targetCandidate.isHiddenSummary());
	}

	private void moveUpwards( final NodeModel selectedNode) {
		if (!checkSelection()) {
			return;
		}
		final MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
		NodeModel selectedParent = selectedNode.getParentNode();
		final List<NodeModel> selectedNodes = Controller.getCurrentController().getSelection().getSortedSelection(true);
		int position;
		final boolean changeSide;
		boolean leftSide = selectedNode.isLeft();
		if (selectedParent.isRoot()) {
			final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
			final Component mapViewComponent = mapViewManager.getMapViewComponent();
			if (!mapViewManager.isLeftTreeSupported(mapViewComponent)) {
				return;
			}
			changeSide = true;
			leftSide = ! leftSide;
			position = selectedParent.getChildCount();
		}
		else {
			final NodeModel grandParent = selectedParent.getParentNode();
			final NodeModel childNode = selectedParent;
			position = grandParent.getIndex(childNode) + 1;
			selectedParent = grandParent;
			changeSide = false;
		}
		for (final NodeModel node : selectedNodes)
			((FreeNode)Controller.getCurrentModeController().getExtension(FreeNode.class)).undoableDeactivateHook(node);
		mapController.moveNodes(selectedNodes, selectedParent, position, leftSide, changeSide);
	}
}
