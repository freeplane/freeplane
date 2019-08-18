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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryLevels;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.MTextController;

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
public class NewParentNode extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	public NewParentNode() {
		super("NewParentNode");
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.extensions.NodeHook#invoke(freeplane.modes.MindMapNode,
	 * java.util.List)
	 */
	public void actionPerformed(final ActionEvent e) {
		final NodeModel selectedNode = Controller.getCurrentModeController().getMapController().getSelectedNode();
		if(selectedNode == null)
			return;
		Collection<NodeModel> unmodifyable = Controller.getCurrentModeController().getMapController().getSelectedNodes();
		final List<NodeModel> selectedNodes = new ArrayList<NodeModel>(unmodifyable.size());
		selectedNodes.addAll(unmodifyable);
		Controller.getCurrentModeController().getMapController().sortNodesByDepth(selectedNodes);
		if (selectedNode.isRoot()) {
			UITools.errorMessage(TextUtils.getText("cannot_add_parent_to_root"));
			return;
		}
		final NodeModel newNode = moveToNewParent(selectedNode, selectedNodes);
		if (newNode == null) {
			return;
		}
		Controller.getCurrentController().getSelection().selectAsTheOnlyOneSelected(newNode);
		Controller.getCurrentController().getViewController().invokeLater(new Runnable() {
			public void run() {
				((MTextController) TextController.getController()).edit(newNode, selectedNode, true, false, false);
			}
		});

	}

	private NodeModel moveToNewParent(final NodeModel selectedNode, final List<NodeModel> selectedNodes) {
		final NodeModel oldParent = selectedNode.getParentNode();
        for (final NodeModel node: selectedNodes) {
            if (node.getParentNode() != oldParent) {
                UITools.errorMessage(TextUtils.getText("cannot_add_parent_diff_parents"));
                return null;
            }
            if (node.isRoot()) {
                UITools.errorMessage(TextUtils.getText("cannot_add_parent_to_root"));
                return null;
            }
        }
        final MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
        final SummaryLevels summaryLevels = new SummaryLevels(oldParent);
        int childPosition = selectedNode.getIndex();
		final NodeModel summaryNode = summaryLevels.findSummaryNode(childPosition);
        if(summaryNode != null){
        	final Collection<NodeModel> summarizedNodes = summaryLevels.summarizedNodes(summaryNode);
        	if(selectedNodes.containsAll(summarizedNodes))
        		childPosition = summaryLevels.findGroupBeginNodeIndex(childPosition);
        	
        }
		final NodeModel newParent = mapController.addNewNode(oldParent, childPosition, selectedNode.isLeft());
        mapController.moveNodesAsChildren(selectedNodes, newParent, false, false);
        return newParent;
	}
}
