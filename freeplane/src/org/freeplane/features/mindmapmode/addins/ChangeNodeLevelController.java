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
package org.freeplane.features.mindmapmode.addins;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.List;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.IMapViewManager;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.mindmapmode.MMapController;

/**
 * @author foltin
 */
public class ChangeNodeLevelController {
	@ActionLocationDescriptor(locations = { "/menu_bar/navigate/nodes" }, //
	accelerator = "control LEFT")
	private class ChangeNodeLevelLeftsAction extends AFreeplaneAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ChangeNodeLevelLeftsAction() {
			super("ChangeNodeLevelLeftsAction", controller);
		}

		public void actionPerformed(final ActionEvent e) {
			final ModeController modeController = getModeController();
			final NodeModel selectedNode = modeController.getMapController().getSelectedNode();
			final IMapViewManager mapViewManager = getController().getMapViewManager();
			final Component mapViewComponent = mapViewManager.getMapViewComponent();
			if (mapViewManager.isLeftTreeSupported(mapViewComponent) && selectedNode.isLeft()) {
				moveDownwards(modeController, selectedNode);
			}
			else {
				moveUpwards(modeController, selectedNode);
			}
		}
	}

	@ActionLocationDescriptor(locations = { "/menu_bar/navigate/nodes" }, //
	accelerator = "control RIGHT")
	private class ChangeNodeLevelRightsAction extends AFreeplaneAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ChangeNodeLevelRightsAction() {
			super("ChangeNodeLevelRightsAction", controller);
		}

		public void actionPerformed(final ActionEvent e) {
			final ModeController modeController = getModeController();
			final NodeModel selectedNode = modeController.getMapController().getSelectedNode();
			final IMapViewManager mapViewManager = getController().getMapViewManager();
			final Component mapViewComponent = mapViewManager.getMapViewComponent();
			if (mapViewManager.isLeftTreeSupported(mapViewComponent) && selectedNode.isLeft()) {
				moveUpwards(modeController, selectedNode);
			}
			else {
				moveDownwards(modeController, selectedNode);
			}
		}
	};

	final private Controller controller;;

	/**
	 *
	 */
	public ChangeNodeLevelController(final Controller controller, final MenuBuilder menuBuilder) {
		this.controller = controller;
		menuBuilder.addAnnotatedAction(new ChangeNodeLevelLeftsAction());
		menuBuilder.addAnnotatedAction(new ChangeNodeLevelRightsAction());
	}

	private boolean checkSelection(final ModeController modeController) {
		final NodeModel selectedNode = modeController.getMapController().getSelectedNode();
		final List<NodeModel> selectedNodes = modeController.getMapController().getSelectedNodes();
		modeController.getController();
		if (selectedNode.isRoot()) {
			UITools.errorMessage(ResourceBundles.getText("cannot_add_parent_to_root"));
			return false;
		}
		final NodeModel selectedParent = selectedNode.getParentNode();
		for (final NodeModel node : selectedNodes) {
			if (node.getParentNode() != selectedParent) {
				UITools.errorMessage(ResourceBundles.getText("cannot_add_parent_diff_parents"));
				return false;
			}
			if (node.isRoot()) {
				UITools.errorMessage(ResourceBundles.getText("cannot_add_parent_to_root"));
				return false;
			}
		}
		return true;
	}

	private void moveDownwards(final ModeController modeController, final NodeModel selectedNode) {
		if (!checkSelection(modeController)) {
			return;
		}
		final NodeModel selectedParent = selectedNode.getParentNode();
		final List<NodeModel> selectedNodes = modeController.getController().getSelection().getSortedSelection(true);
		final MMapController mapController = (MMapController) modeController.getMapController();
		final int ownPosition = selectedParent.getChildPosition(selectedNode);
		NodeModel directSibling = null;
		for (int i = ownPosition - 1; i >= 0; --i) {
			final NodeModel sibling = (NodeModel) selectedParent.getChildAt(i);
			if ((!selectedNodes.contains(sibling)) && selectedNode.isLeft() == sibling.isLeft()) {
				directSibling = sibling;
				break;
			}
		}
		if (directSibling == null) {
			for (int i = ownPosition + 1; i < selectedParent.getChildCount(); ++i) {
				final NodeModel sibling = (NodeModel) selectedParent.getChildAt(i);
				if ((!selectedNodes.contains(sibling)) && selectedNode.isLeft() == sibling.isLeft()) {
					directSibling = sibling;
					break;
				}
			}
		}
		if (directSibling != null) {
			for (final NodeModel node : selectedNodes) {
				mapController.moveNode(node, directSibling, directSibling.getChildCount());
			}
			modeController.getMapController().selectMultipleNodes(selectedNode, selectedNodes);
		}
	}

	private void moveUpwards(final ModeController modeController, final NodeModel selectedNode) {
		if (!checkSelection(modeController)) {
			return;
		}
		final MMapController mapController = (MMapController) modeController.getMapController();
		NodeModel selectedParent = selectedNode.getParentNode();
		final List<NodeModel> selectedNodes = modeController.getController().getSelection().getSortedSelection(true);
		int position;
		final boolean changeSide;
		if (selectedParent.isRoot()) {
			final IMapViewManager mapViewManager = modeController.getController().getMapViewManager();
			final Component mapViewComponent = mapViewManager.getMapViewComponent();
			if (!mapViewManager.isLeftTreeSupported(mapViewComponent)) {
				return;
			}
			position = selectedParent.getChildCount() - 1;
			changeSide = true;
		}
		else {
			final NodeModel grandParent = selectedParent.getParentNode();
			position = grandParent.getChildPosition(selectedParent) + 1;
			selectedParent = grandParent;
			changeSide = false;
		}
		for (final NodeModel node : selectedNodes) {
			mapController.moveNode(node, selectedParent, position, !node.isLeft(), changeSide);
			if (!changeSide) {
				position++;
			}
		}
		mapController.selectMultipleNodes(selectedNode, selectedNodes);
	}
}
