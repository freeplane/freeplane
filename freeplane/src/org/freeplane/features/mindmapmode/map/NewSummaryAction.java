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
package org.freeplane.features.mindmapmode.map;

import java.awt.event.ActionEvent;
import java.util.List;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.IMapSelection;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.common.map.FirstGroupNode;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.map.SummaryNode;

@EnabledAction(checkOnNodeChange=true)
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
		if(! check())
			return;
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

	@Override
    public void setEnabled() {
		setEnabled(check());
		
   }

	private boolean check() {
		start = -1;
		end = -1;
		summaryLevel = -1;
	    final ModeController modeController = Controller.getCurrentModeController();
		final IMapSelection selection = modeController.getController().getSelection();
		
		// more than 2 nodes selected
		if(selection.size() > 2){
			return false;
		}
		final List<NodeModel> list = selection.getSelection();
		final NodeModel node0 = list.get(0);
		
		// root node selected
		if(node0.isRoot()){
			return false;
		}
		final NodeModel node1;
		if(selection.size() == 1){
			node1 = node0;
		}
		else{
			node1 = list.get(1);
		}
		final NodeModel parentNode = node0.getParentNode();
		
		// different parents
		if(! parentNode.equals(node1.getParentNode())){
			return false;
		}
		final boolean isLeft = node0.isLeft();
		// different sides
		if(isLeft!=node1.isLeft()){
			return false;
		}
		final int index0 = parentNode.getIndex(node0);
		final int index1 = parentNode.getIndex(node1);
		final NodeModel lastNode;
		if(index0 < index1){
			start = index0;
			end = index1;
			lastNode = node1;
		}
		else{
			start = index1;
			end = index0;
			lastNode = node0;
		}
		// last node is a group node
		if(node0 != node1 && SummaryNode.isFirstGroupNode(lastNode))
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
		
		summaryLevel = SummaryNode.getSummaryLevel(node0);
		
		// selected nodes have different summary levels
		if (summaryLevel != SummaryNode.getSummaryLevel(node1))
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
