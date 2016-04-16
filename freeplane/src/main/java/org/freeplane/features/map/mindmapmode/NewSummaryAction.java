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
package org.freeplane.features.map.mindmapmode;

import java.awt.event.ActionEvent;
import java.util.List;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeRelativePath;
import org.freeplane.features.map.SummaryNode;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

class NewSummaryAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int start;
	private int end;
	private int summaryLevel;

	public NewSummaryAction() {
		super("NewSummaryAction");
	}

	public void actionPerformed(final ActionEvent e) {
		if(! addNewSummaryNodeStartEditing()){
			UITools.errorMessage(TextUtils.getText("summary_not_possible"));
		}
	}

	private boolean addNewSummaryNodeStartEditing() {
	    final ModeController modeController = Controller.getCurrentModeController();
		final IMapSelection selection = modeController.getController().getSelection();

		final List<NodeModel> sortedSelection = selection.getSortedSelection(false);
		
		final NodeModel firstNode = sortedSelection.get(0);
		final NodeModel lastNode = sortedSelection.get(sortedSelection.size()-1);
		
		final boolean isLeft = firstNode.isLeft();
		// different sides
		if(isLeft!=lastNode.isLeft()){
			return false;
		}
		final NodeModel parentNode = firstNode.getParentNode();
		if(parentNode == null)
			return false;
		final NodeModel lastParent = lastNode.getParentNode();
		if(lastParent == null)
			return false;
		if(parentNode.equals(lastParent))
			return addNewSummaryNodeStartEditing(firstNode, lastNode);
		else {
			final NodeRelativePath nodeRelativePath = new NodeRelativePath(firstNode, lastNode);
			NodeModel commonAncestor = nodeRelativePath.commonAncestor();
			if (commonAncestor == firstNode || commonAncestor == lastNode)
				return false;
			final NodeModel newFirstNode = nodeRelativePath.beginPathElement(1);
			final NodeModel newLastNode = nodeRelativePath.endPathElement(1);
			return addNewSummaryNodeStartEditing(newFirstNode, newLastNode);
		}
    }

	private boolean addNewSummaryNodeStartEditing(final NodeModel firstNode, final NodeModel lastNode) {

		final NodeModel parentNode = firstNode.getParentNode();
		final ModeController modeController = Controller.getCurrentModeController();
		final boolean isLeft = firstNode.isLeft();
		start = parentNode.getIndex(firstNode);
		end = parentNode.getIndex(lastNode);
		if(end < start){
			int temp = end;
			end = start;
			start = temp;
		}
		
		summaryLevel = SummaryNode.getSummaryLevel(firstNode);
		
		// selected nodes have different summary levels
		if (summaryLevel != SummaryNode.getSummaryLevel(lastNode))
			return false;
		int level = summaryLevel;
		for(int i = start+1; i < end; i++){
			NodeModel node = (NodeModel) parentNode.getChildAt(i);
			if(isLeft != node.isLeft())
				continue;
			if(SummaryNode.isSummaryNode(node))
				level++;
			else
				level = 0;
			// There is a higher summary node between the selected nodes
			if(level > summaryLevel)
				return false;
		}
		((MMapController) modeController.getMapController()).addNewSummaryNodeStartEditing(parentNode, start, end, summaryLevel, isLeft);
		return true;
	}
}
