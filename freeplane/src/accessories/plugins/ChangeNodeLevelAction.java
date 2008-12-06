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
package accessories.plugins;

import java.awt.datatransfer.Transferable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.freeplane.controller.Freeplane;
import org.freeplane.main.Tools;
import org.freeplane.map.clipboard.mindmapmode.MClipboardController;
import org.freeplane.map.tree.NodeModel;

import deprecated.freemind.modes.mindmapmode.hooks.MindMapNodeHookAdapter;

/**
 * @author foltin
 */
public class ChangeNodeLevelAction extends MindMapNodeHookAdapter {
	/**
	 *
	 */
	public ChangeNodeLevelAction() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.extensions.NodeHook#invoke(freemind.modes.MindMapNode,
	 * java.util.List)
	 */
	@Override
	public void invoke(final NodeModel rootNode) {
		NodeModel selectedNode;
		List selectedNodes;
		{
			final NodeModel focussed = getMindMapController().getSelectedNode();
			final List selecteds = getMindMapController().getSelectedNodes();
			selectedNode = focussed;
			selectedNodes = selecteds;
		}
		getMindMapController().getMapController().sortNodesByDepth(
		    selectedNodes);
		if (selectedNode.isRoot()) {
			Freeplane.getController().errorMessage(
			    getResourceString("cannot_add_parent_to_root"));
			return;
		}
		final boolean upwards = Tools.safeEquals("left",
		    getResourceString("action_type")) != selectedNode.isLeft();
		final NodeModel selectedParent = selectedNode.getParentNode();
		for (final Iterator it = selectedNodes.iterator(); it.hasNext();) {
			final NodeModel node = (NodeModel) it.next();
			if (node.getParentNode() != selectedParent) {
				Freeplane.getController().errorMessage(
				    getResourceString("cannot_add_parent_diff_parents"));
				return;
			}
			if (node == rootNode) {
				Freeplane.getController().errorMessage(
				    getResourceString("cannot_add_parent_to_root"));
				return;
			}
		}
		final String selectedNodeId = selectedNode.createID();
		final Vector selectedNodesId = new Vector();
		for (final Iterator iter = selectedNodes.iterator(); iter.hasNext();) {
			final NodeModel node = (NodeModel) iter.next();
			selectedNodesId.add(node.createID());
		}
		final MClipboardController clipboardController = (MClipboardController) getMindMapController()
		    .getClipboardController();
		if (upwards) {
			if (selectedParent.isRoot()) {
				final boolean isLeft = selectedNode.isLeft();
				final Transferable copy = clipboardController
				    .cut(selectedNodes);
				((MClipboardController) getMindMapController()
				    .getClipboardController()).paste(copy, selectedParent,
				    false, (!isLeft));
				select(selectedNodeId, selectedNodesId);
				return;
			}
			final NodeModel grandParent = selectedParent.getParentNode();
			final int parentPosition = grandParent
			    .getChildPosition(selectedParent);
			final boolean isLeft = selectedParent.isLeft();
			final Transferable copy = clipboardController.cut(selectedNodes);
			if (parentPosition == grandParent.getChildCount() - 1) {
				((MClipboardController) getMindMapController()
				    .getClipboardController()).paste(copy, grandParent, false,
				    isLeft);
			}
			else {
				((MClipboardController) getMindMapController()
				    .getClipboardController()).paste(copy,
				    ((NodeModel) grandParent.getChildAt(parentPosition + 1)),
				    true, isLeft);
			}
			select(selectedNodeId, selectedNodesId);
		}
		else {
			final int ownPosition = selectedParent
			    .getChildPosition(selectedNode);
			NodeModel directSibling = null;
			for (int i = ownPosition - 1; i >= 0; --i) {
				final NodeModel sibling = (NodeModel) selectedParent
				    .getChildAt(i);
				if ((!selectedNodes.contains(sibling))
				        && selectedNode.isLeft() == sibling.isLeft()) {
					directSibling = sibling;
					break;
				}
			}
			if (directSibling == null) {
				for (int i = ownPosition + 1; i < selectedParent
				    .getChildCount(); ++i) {
					final NodeModel sibling = (NodeModel) selectedParent
					    .getChildAt(i);
					if ((!selectedNodes.contains(sibling))
					        && selectedNode.isLeft() == sibling.isLeft()) {
						directSibling = sibling;
						break;
					}
				}
			}
			if (directSibling != null) {
				final Transferable copy = clipboardController
				    .cut(selectedNodes);
				((MClipboardController) getMindMapController()
				    .getClipboardController()).paste(copy, directSibling,
				    false, directSibling.isLeft());
				select(selectedNodeId, selectedNodesId);
				return;
			}
		}
	}

	private void select(final String selectedNodeId, final List selectedNodesIds) {
		final NodeModel newInstanceOfSelectedNode = getMindMapController()
		    .getMapController().getNodeFromID(selectedNodeId);
		final List newSelecteds = new LinkedList();
		for (final Iterator iter = selectedNodesIds.iterator(); iter.hasNext();) {
			final String nodeId = (String) iter.next();
			newSelecteds.add(getMindMapController().getMapController()
			    .getNodeFromID(nodeId));
		}
		getMindMapController().selectMultipleNodes(newInstanceOfSelectedNode,
		    newSelecteds);
	}
}
