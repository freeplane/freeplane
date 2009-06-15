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
import java.util.Iterator;
import java.util.List;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.mindmapmode.MMapController;

/**
 * @author foltin The original version was sent by Stephen Viles (sviles) https:
 *         group_id=7118 Initial Comment: The "New Parent Node" action creates a
 *         node as a parent of one or more selected nodes. If more than one node
 *         is selected, the selected nodes must all have the same parent -- this
 *         restriction is imposed to make the action easier to understand and to
 *         undo manually, and could potentially be removed when we get automated
 *         undo. The root node must not be one of the selected nodes. I find
 *         this action useful when I need to add an extra level of grouping in
 *         the middle of an existing hierarchy. It is quicker than adding a new
 *         node at the same level and then cutting-and-pasting the child nodes.
 *         The code simply performs these actions in sequence, after validating
 *         the selected nodes.
 */
@ActionLocationDescriptor(locations = { "/menu_bar/insert/nodes" }, //
accelerator = "shift INSERT")
public class NewParentNode extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	public NewParentNode(final Controller controller) {
		super("NewParentNode", controller);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.extensions.NodeHook#invoke(freeplane.modes.MindMapNode,
	 * java.util.List)
	 */
	public void actionPerformed(final ActionEvent e) {
		final NodeModel focussed = getModeController().getMapController().getSelectedNode();
		final List selecteds = getModeController().getMapController().getSelectedNodes();
		final NodeModel selectedNode = focussed;
		final List selectedNodes = selecteds;
		getModeController().getMapController().sortNodesByDepth(selectedNodes);
		if (focussed.isRoot()) {
			UITools.errorMessage(ResourceBundles.getText("cannot_add_parent_to_root"));
			return;
		}
		final NodeModel newNode = moveToNewParent(selectedNode, selectedNodes);
		if (newNode == null) {
			return;
		}
		getController().getSelection().selectAsTheOnlyOneSelected(newNode);
	}

	private NodeModel moveToNewParent(final NodeModel selectedNode, final List<NodeModel> selectedNodes) {
		final NodeModel oldParent = selectedNode.getParentNode();
		final int childPosition = oldParent.getChildPosition(selectedNode);
		final NodeModel newParent = ((MMapController) getModeController().getMapController()).addNewNode(oldParent,
		    childPosition, selectedNode.isLeft());
		return moveToOtherNode(selectedNodes, oldParent, newParent);
	}

	private NodeModel moveToOtherNode(final List<NodeModel> selectedNodes, final NodeModel oldParent,
	                                  final NodeModel newParent) {
		for (final Iterator it = selectedNodes.iterator(); it.hasNext();) {
			final NodeModel node = (NodeModel) it.next();
			if (node.getParentNode() != oldParent) {
				UITools.errorMessage(ResourceBundles.getText("cannot_add_parent_diff_parents"));
				return null;
			}
			if (node.isRoot()) {
				UITools.errorMessage(ResourceBundles.getText("cannot_add_parent_to_root"));
				return null;
			}
		}
		final MMapController mapController = (MMapController) getModeController().getMapController();
		for (final NodeModel node : selectedNodes) {
			mapController.moveNodeAsChild(node, newParent, false, false);
		}
		return newParent;
	}
}
