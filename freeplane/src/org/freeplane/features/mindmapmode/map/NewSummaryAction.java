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

import javax.swing.tree.TreeNode;

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

	public NewSummaryAction() {
		super("NewSummaryAction");
	}

	public void actionPerformed(final ActionEvent e) {
		if(! check())
			return;
		final ModeController modeController = Controller.getCurrentModeController();
		List<NodeModel> selection = modeController.getController().getSelection().getSelection();
		NodeModel selected = selection.get(0);
		final NodeModel parentNode = selected.getParentNode();
		final boolean left = selected.isLeft();
		final MMapController mapController = (MMapController) modeController.getMapController();
		final NodeModel firstNode;
		final NodeModel lastNode;
		final int lastNodeIndex;
		if(selection.size() == 1){
			firstNode = selected;
			lastNode = selected;
			lastNodeIndex = parentNode.getIndex(lastNode);
		}
		else{
			final NodeModel node0 = selection.get(0);
			final int index0 = parentNode.getIndex(node0);
			final NodeModel node1 = selection.get(1);
			final int index1 = parentNode.getIndex(node1);
			if(index0 < index1){
				firstNode = node0;
				lastNode = node1;
				lastNodeIndex = index1;
			}
			else{
				firstNode = node1;
				lastNode = node0;
				lastNodeIndex = index0;
			}
		}
		final NodeModel newNode = mapController.addNewNode(parentNode, lastNodeIndex+1, left);
		final SummaryNode summary = (SummaryNode) modeController.getExtension(SummaryNode.class);
		summary.undoableActivateHook(newNode, summary);
		final FirstGroupNode firstGroup = (FirstGroupNode) modeController.getExtension(FirstGroupNode.class);
		firstGroup.undoableActivateHook(firstNode, firstGroup);
		mapController.select(newNode);
	}

	@Override
    public void setEnabled() {
		setEnabled(check());
		
   }

	private boolean check() {
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
		final int start;
		final int end;
		final NodeModel lastNode;
		if(index0 < index1){
			start = index0 + 1;
			end = index1;
			lastNode = node1;
		}
		else{
			start = index1 + 1;
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
		
		final int summaryLevel = SummaryNode.getSummaryLevel(node0);
		
		// selected nodes have different summary levels
		if (summaryLevel != SummaryNode.getSummaryLevel(node1))
			return false;
		int level = summaryLevel;
		for(int i = start; i < end; i++){
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
			
			// There is a first group node between the selected nodes
			if(level == summaryLevel && SummaryNode.isFirstGroupNode(node))
				return false;
		}
		return true;
    }
}
