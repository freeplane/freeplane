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
		if(! check()){
			UITools.errorMessage(TextUtils.getText("summary_not_possible"));
			return;
		}
		final MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
		mapController.addNewSummaryNodeStartEditing(summaryLevel, start, end);
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
