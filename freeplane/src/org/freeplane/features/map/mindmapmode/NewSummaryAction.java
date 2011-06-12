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

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.IMapSelection;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.FirstGroupNode;
import org.freeplane.features.map.ModeController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNode;

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
		if(! check()){
			UITools.errorMessage(TextUtils.getText("summary_not_possible"));
			return;
		}
		final int summaryLevel = this.summaryLevel;
		final int start = this.start;
		final int end = this.end;
		final ModeController modeController = Controller.getCurrentModeController();
		List<NodeModel> selection = modeController.getController().getSelection().getSelection();
		NodeModel selected = selection.get(0);
		final NodeModel parentNode = selected.getParentNode();
		final boolean isLeft = selected.isLeft();
		final MMapController mapController = (MMapController) modeController.getMapController();
		final NodeModel newNode = mapController.addNewNode(parentNode, end+1, isLeft);
		final SummaryNode summary = (SummaryNode) modeController.getExtension(SummaryNode.class);
		summary.undoableActivateHook(newNode, summary);
		final FirstGroupNode firstGroup = (FirstGroupNode) modeController.getExtension(FirstGroupNode.class);
		final NodeModel firstNode = (NodeModel) parentNode.getChildAt(start);
		firstGroup.undoableActivateHook(firstNode, firstGroup);
		int level = summaryLevel;
		for(int i = start+1; i < end; i++){
			NodeModel node = (NodeModel) parentNode.getChildAt(i);
			if(isLeft != node.isLeft())
				continue;
			if(SummaryNode.isSummaryNode(node))
				level++;
			else
				level = 0;
			if(level == summaryLevel && SummaryNode.isFirstGroupNode(node))
				firstGroup.undoableActivateHook(node, firstGroup);
		}
		mapController.select(newNode);
	}

	private boolean check() {
		start = -1;
		end = -1;
		summaryLevel = -1;
	    final ModeController modeController = Controller.getCurrentModeController();
		final IMapSelection selection = modeController.getController().getSelection();

		final List<NodeModel> sortedSelection = selection.getSortedSelection(false);
		
		final NodeModel firstNode = sortedSelection.get(0);
		
		final NodeModel parentNode = firstNode.getParentNode();
		// root node selected
		if(parentNode == null)
			return false;
		
		final NodeModel lastNode = sortedSelection.get(sortedSelection.size()-1);
		// different parents
		if(! parentNode.equals(lastNode.getParentNode())){
			return false;
		}
		final boolean isLeft = firstNode.isLeft();
		// different sides
		if(isLeft!=lastNode.isLeft()){
			return false;
		}
		start = parentNode.getIndex(firstNode);
		end = parentNode.getIndex(lastNode);
		
		// last node is a group node
		if(firstNode != lastNode && SummaryNode.isFirstGroupNode(lastNode))
			return false;
		
		// last node is already followed by a summary node
		for(int i = end+1; i < parentNode.getChildCount(); i++){
			final NodeModel next = (NodeModel) parentNode.getChildAt(i);
			if(next.isLeft() == isLeft){
				if(SummaryNode.isSummaryNode(next))
					return false;
				break;
			}
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
		return true;
    }
}
