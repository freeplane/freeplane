/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.features.common.filter;

import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.IMapSelection;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.common.filter.condition.ASelectableCondition;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.text.TextController.Direction;

/**
 * @author Dimitry Polivaev
 * Mar 30, 2009
 */
final class QuickFindAllAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private final FilterConditionEditor filterEditor;
	private final FilterController filterController;


	/**
	 * @param filterController
	 * @param quickEditor 
	 */
	QuickFindAllAction(final FilterController filterController, FilterConditionEditor quickEditor) {
		super("QuickFindAllAction");
		this.filterController = filterController;
		this.filterEditor = quickEditor;
	}

	public void actionPerformed(final ActionEvent e) {
		final ASelectableCondition condition = filterEditor.getCondition();
		if(condition == null){
			return;
		}
		final IMapSelection selection = Controller.getCurrentController().getSelection();
		final NodeModel selected = selection.getSelected();
		boolean nodeFound = condition.checkNode(selected);
		if(nodeFound){
			selection.selectAsTheOnlyOneSelected(selected);
		}
		NodeModel next = selected;
		for(;;){
			next = filterController.findNext(next, selected, Direction.FORWARD, condition);
			if(next == null){
				return;
			}
			if(nodeFound){
				selection.toggleSelected(next);
			}
			else{
				selection.selectAsTheOnlyOneSelected(next);
				nodeFound = true;
			}
		}
	}
}
