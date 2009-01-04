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

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.ActionDescriptor;
import org.freeplane.core.ui.FreeplaneAction;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.features.common.clipboard.ClipboardController;
import org.freeplane.features.mindmapmode.clipboard.MClipboardController;

/**
 * @author foltin
 */
public class ChangeNodeLevelAction {
	@ActionDescriptor(tooltip = "accessories/plugins/ChangeNodeLevelAction_right.properties_documentation", //
	name = "accessories/plugins/ChangeNodeLevelAction_right.properties_name", //
	keyStroke = "keystroke_accessories/plugins/ChangeNodeLevelAction_right.properties_key", //
	locations = { "/menu_bar/navigate/nodes" })
	private class ChangeNodeLevelDownwardsAction extends FreeplaneAction {
		public void actionPerformed(final ActionEvent e) {
			ChangeNodeLevelAction.this.actionPerformed(getModeController(), false);
		}
	};

	@ActionDescriptor(tooltip = "accessories/plugins/ChangeNodeLevelAction_left.properties_documentation", //
	name = "accessories/plugins/ChangeNodeLevelAction_left.properties_name", //
	keyStroke = "keystroke_accessories/plugins/ChangeNodeLevelAction_left.properties_key", //
	locations = { "/menu_bar/navigate/nodes" })
	private class ChangeNodeLevelUpwardsAction extends FreeplaneAction {
		public void actionPerformed(final ActionEvent e) {
			ChangeNodeLevelAction.this.actionPerformed(getModeController(), true);
		}
	};

	/**
	 *
	 */
	public ChangeNodeLevelAction(final MenuBuilder menuBuilder) {
		menuBuilder.addAnnotatedAction(new ChangeNodeLevelUpwardsAction());
		menuBuilder.addAnnotatedAction(new ChangeNodeLevelDownwardsAction());
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.extensions.NodeHook#invoke(freemind.modes.MindMapNode,
	 * java.util.List)
	 */
	public void actionPerformed(final ModeController modeController, final boolean upwards) {
		NodeModel selectedNode;
		List selectedNodes;
		{
			final NodeModel focussed = modeController.getMapController().getSelectedNode();
			final List selecteds = modeController.getMapController().getSelectedNodes();
			selectedNode = focussed;
			selectedNodes = selecteds;
		}
		modeController.getMapController().sortNodesByDepth(selectedNodes);
		if (selectedNode.isRoot()) {
			Controller.getController()
			    .errorMessage(Controller.getText("cannot_add_parent_to_root"));
			return;
		}
		final NodeModel selectedParent = selectedNode.getParentNode();
		for (final Iterator it = selectedNodes.iterator(); it.hasNext();) {
			final NodeModel node = (NodeModel) it.next();
			if (node.getParentNode() != selectedParent) {
				Controller.getController().errorMessage(
				    Controller.getText("cannot_add_parent_diff_parents"));
				return;
			}
			if (node.isRoot()) {
				Controller.getController().errorMessage(
				    Controller.getText("cannot_add_parent_to_root"));
				return;
			}
		}
		final String selectedNodeId = selectedNode.createID();
		final Vector selectedNodesId = new Vector();
		for (final Iterator iter = selectedNodes.iterator(); iter.hasNext();) {
			final NodeModel node = (NodeModel) iter.next();
			selectedNodesId.add(node.createID());
		}
		final MClipboardController clipboardController = (MClipboardController) ClipboardController
		    .getController(modeController);
		if (upwards) {
			if (selectedParent.isRoot()) {
				final boolean isLeft = selectedNode.isLeft();
				final Transferable copy = clipboardController.cut(selectedNodes);
				((MClipboardController) ClipboardController.getController(modeController)).paste(
				    copy, selectedParent, false, (!isLeft));
				select(modeController, selectedNodeId, selectedNodesId);
				return;
			}
			final NodeModel grandParent = selectedParent.getParentNode();
			final int parentPosition = grandParent.getChildPosition(selectedParent);
			final boolean isLeft = selectedParent.isLeft();
			final Transferable copy = clipboardController.cut(selectedNodes);
			if (parentPosition == grandParent.getChildCount() - 1) {
				((MClipboardController) ClipboardController.getController(modeController)).paste(
				    copy, grandParent, false, isLeft);
			}
			else {
				((MClipboardController) ClipboardController.getController(modeController)).paste(
				    copy, ((NodeModel) grandParent.getChildAt(parentPosition + 1)), true, isLeft);
			}
			select(modeController, selectedNodeId, selectedNodesId);
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
					if ((!selectedNodes.contains(sibling))
					        && selectedNode.isLeft() == sibling.isLeft()) {
						directSibling = sibling;
						break;
					}
				}
			}
			if (directSibling != null) {
				final Transferable copy = clipboardController.cut(selectedNodes);
				((MClipboardController) ClipboardController.getController(modeController)).paste(
				    copy, directSibling, false, directSibling.isLeft());
				select(modeController, selectedNodeId, selectedNodesId);
				return;
			}
		}
	}

	private void select(final ModeController modeController, final String selectedNodeId,
	                    final List selectedNodesIds) {
		final NodeModel newInstanceOfSelectedNode = modeController.getMapController()
		    .getNodeFromID(selectedNodeId);
		final List newSelecteds = new LinkedList();
		for (final Iterator iter = selectedNodesIds.iterator(); iter.hasNext();) {
			final String nodeId = (String) iter.next();
			newSelecteds.add(modeController.getMapController().getNodeFromID(nodeId));
		}
		modeController.getMapController().selectMultipleNodes(newInstanceOfSelectedNode,
		    newSelecteds);
	}
}
