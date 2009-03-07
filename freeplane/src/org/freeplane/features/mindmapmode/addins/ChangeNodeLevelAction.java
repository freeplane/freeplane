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

import java.awt.event.ActionEvent;
import java.util.List;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.FreeplaneResourceBundle;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionDescriptor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.features.mindmapmode.MMapController;

/**
 * @author foltin
 */
public class ChangeNodeLevelAction {
	@ActionDescriptor(tooltip = "accessories/plugins/ChangeNodeLevelAction_right.properties_documentation", //
	name = "accessories/plugins/ChangeNodeLevelAction_right.properties_name", //
	keyStroke = "keystroke_accessories/plugins/ChangeNodeLevelAction_right.properties_key", //
	locations = { "/menu_bar/navigate/nodes" })
	private class ChangeNodeLevelDownwardsAction extends AFreeplaneAction {
		public ChangeNodeLevelDownwardsAction() {
			super(controller);
		}

		public void actionPerformed(final ActionEvent e) {
			ChangeNodeLevelAction.this.actionPerformed(getModeController(), false);
		}
	}

	@ActionDescriptor(tooltip = "accessories/plugins/ChangeNodeLevelAction_left.properties_documentation", //
	name = "accessories/plugins/ChangeNodeLevelAction_left.properties_name", //
	keyStroke = "keystroke_accessories/plugins/ChangeNodeLevelAction_left.properties_key", //
	locations = { "/menu_bar/navigate/nodes" })
	private class ChangeNodeLevelUpwardsAction extends AFreeplaneAction {
		public ChangeNodeLevelUpwardsAction() {
			super(controller);
		}

		public void actionPerformed(final ActionEvent e) {
			ChangeNodeLevelAction.this.actionPerformed(getModeController(), true);
		}
	};

	final private Controller controller;;

	/**
	 *
	 */
	public ChangeNodeLevelAction(final Controller controller, final MenuBuilder menuBuilder) {
		this.controller = controller;
		menuBuilder.addAnnotatedAction(new ChangeNodeLevelUpwardsAction());
		menuBuilder.addAnnotatedAction(new ChangeNodeLevelDownwardsAction());
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.extensions.NodeHook#invoke(freeplane.modes.MindMapNode,
	 * java.util.List)
	 */
	public void actionPerformed(final ModeController modeController, final boolean upwards) {
		final MMapController mapController = (MMapController) modeController.getMapController();
		NodeModel selectedNode = mapController.getSelectedNode();
		List<NodeModel> selectedNodes = mapController.getSelectedNodes();
		mapController.sortNodesByDepth(selectedNodes);
		final Controller controller = modeController.getController();
		if (selectedNode.isRoot()) {
			controller.errorMessage(FreeplaneResourceBundle.getText("cannot_add_parent_to_root"));
			return;
		}
		final NodeModel selectedParent = selectedNode.getParentNode();
		for (final NodeModel node : selectedNodes) {
			if (node.getParentNode() != selectedParent) {
				controller.errorMessage(FreeplaneResourceBundle.getText("cannot_add_parent_diff_parents"));
				return;
			}
			if (node.isRoot()) {
				controller.errorMessage(FreeplaneResourceBundle.getText("cannot_add_parent_to_root"));
				return;
			}
		}
		if (upwards) {
			if (selectedParent.isRoot()) {
				final boolean isLeft = selectedNode.isLeft();
				for (final NodeModel node : selectedNodes) {
					mapController.moveNode(node, selectedParent);
				}
				return;
			}
			NodeModel grandParent = selectedParent.getParentNode();
			final int parentPosition = grandParent.getChildPosition(selectedParent);
			if (parentPosition != grandParent.getChildCount() - 1) {
				grandParent = ((NodeModel) grandParent.getChildAt(parentPosition + 1));
			}
			for (final NodeModel node : selectedNodes) {
				mapController.moveNode(node, grandParent);
			}
		}
		else {
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
					mapController.moveNode(node, directSibling);
				}
				return;
			}
		}
	}
}
