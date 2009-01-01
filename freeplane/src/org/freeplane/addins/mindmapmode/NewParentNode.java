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
package org.freeplane.addins.mindmapmode;

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.map.NodeModel;
import org.freeplane.core.ui.ActionDescriptor;
import org.freeplane.core.ui.FreeplaneAction;
import org.freeplane.map.clipboard.mindmapmode.MClipboardController;
import org.freeplane.modes.mindmapmode.MMapController;
import org.freeplane.view.map.MapView;

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
@ActionDescriptor(tooltip = "accessories/plugins/NewParentNode.properties_documentation", //
name = "accessories/plugins/NewParentNode.properties_name", //
keyStroke = "keystroke_accessories/plugins/NewParentNode.properties_key", //
iconPath = "accessories/plugins/icons/stock_text_indent.png", //
locations = { "/menu_bar/insert/nodes" })
public class NewParentNode extends FreeplaneAction {
	/**
	 *
	 */
	public NewParentNode() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.extensions.NodeHook#invoke(freemind.modes.MindMapNode,
	 * java.util.List)
	 */
	public void actionPerformed(final ActionEvent e) {
		// TODO Auto-generated method stub
		final MapView mapView = getModeController().getMapView();
		final NodeModel focussed = getModeController().getSelectedNode();
		final List selecteds = getModeController().getSelectedNodes();
		final NodeModel selectedNode = focussed;
		final List selectedNodes = selecteds;
		getModeController().getMapController().sortNodesByDepth(selectedNodes);
		if (focussed.isRoot()) {
			Controller.getController()
			    .errorMessage(Controller.getText("cannot_add_parent_to_root"));
			return;
		}
		final NodeModel newNode = moveToNewParent(selectedNode, selectedNodes);
		if (newNode == null) {
			return;
		}
		mapView.selectAsTheOnlyOneSelected(mapView.getNodeView(newNode));
		mapView.repaint();
	}

	private NodeModel moveToNewParent(final NodeModel selectedNode, final List selectedNodes) {
		final NodeModel selectedParent = selectedNode.getParentNode();
		final int childPosition = selectedParent.getChildPosition(selectedNode);
		final NodeModel newNode = ((MMapController) getModeController().getMapController())
		    .addNewNode(selectedParent, childPosition, selectedNode.isLeft());
		return moveToOtherNode(selectedNodes, selectedParent, newNode);
	}

	private NodeModel moveToOtherNode(final List selectedNodes, final NodeModel selectedParent,
	                                  final NodeModel newNode) {
		for (final Iterator it = selectedNodes.iterator(); it.hasNext();) {
			final NodeModel node = (NodeModel) it.next();
			if (node.getParentNode() != selectedParent) {
				Controller.getController().errorMessage(
				    Controller.getText("cannot_add_parent_diff_parents"));
				return null;
			}
			if (node.isRoot()) {
				Controller.getController().errorMessage(
				    Controller.getText("cannot_add_parent_to_root"));
				return null;
			}
		}
		final Transferable copy = ((MClipboardController) getModeController()
		    .getClipboardController()).cut(selectedNodes);
		((MClipboardController) getModeController().getClipboardController()).paste(copy, newNode);
		getModeController().getMapController().nodeChanged(selectedParent);
		return newNode;
	}
}
