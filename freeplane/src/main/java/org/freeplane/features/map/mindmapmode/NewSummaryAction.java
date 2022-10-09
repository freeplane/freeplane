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

	public NewSummaryAction() {
		super("NewSummaryAction");
	}

	public void actionPerformed(final ActionEvent e) {
		addNewSummaryNodeStartEditing();
	}

	private void addNewSummaryNodeStartEditing() {
	    final ModeController modeController = Controller.getCurrentModeController();
		final IMapSelection selection = modeController.getController().getSelection();

		final List<NodeModel> sortedSelection = selection.getSortedSelection(false);
		
		final NodeModel firstNode = sortedSelection.get(0);
		final NodeModel lastNode = sortedSelection.get(sortedSelection.size()-1);
		
		NodeModel selectionRoot = selection.getSelectionRoot();
		final boolean isTopOrLeft = firstNode.isTopOrLeft(selectionRoot);
		// different sides
		if(isTopOrLeft!=lastNode.isTopOrLeft(selectionRoot)){
			UITools.errorMessage(TextUtils.getText("summary_not_possible")); 
			return;
		}
		final NodeModel parentNode = firstNode.getParentNode();
		if(parentNode == null) {
			UITools.errorMessage(TextUtils.getText("summary_not_possible")); 
			return;
		}
		final NodeModel lastParent = lastNode.getParentNode();
		if(lastParent == null) {
			UITools.errorMessage(TextUtils.getText("summary_not_possible")); 
			return;
		}
		if(parentNode.equals(lastParent))
			addNewSummaryNodeStartEditing(selectionRoot, firstNode, lastNode);
		else {
			final NodeRelativePath nodeRelativePath = new NodeRelativePath(firstNode, lastNode);
			NodeModel commonAncestor = nodeRelativePath.commonAncestor();
			if (commonAncestor == firstNode || commonAncestor == lastNode) {
				UITools.errorMessage(TextUtils.getText("summary_not_possible")); 
				return;
			}
			final NodeModel newFirstNode = nodeRelativePath.beginPathElement(1);
			final NodeModel newLastNode = nodeRelativePath.endPathElement(1);
			addNewSummaryNodeStartEditing(selectionRoot, newFirstNode, newLastNode);
		}
    }

	private void addNewSummaryNodeStartEditing(final NodeModel selectionRoot, final NodeModel firstNode, final NodeModel lastNode) {

		final NodeModel parentNode = firstNode.getParentNode();
		final ModeController modeController = Controller.getCurrentModeController();
		final boolean isTopOrLeft = firstNode.isTopOrLeft(selectionRoot);
		int start = parentNode.getIndex(firstNode);
		int end = parentNode.getIndex(lastNode);
		if(end < start){
			int temp = end;
			end = start;
			start = temp;
		}
		
		((MMapController) modeController.getMapController()).addNewSummaryNodeStartEditing(selectionRoot, parentNode, start, end, isTopOrLeft);
	}
}
