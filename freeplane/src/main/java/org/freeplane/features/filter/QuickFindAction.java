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
package org.freeplane.features.filter;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.MapController.Direction;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 * Mar 30, 2009
 */
final class QuickFindAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private final FilterConditionEditor filterEditor;
	private final FilterController filterController;
	private final Direction direction;


	/**
	 * @param filterController
	 * @param quickEditor 
	 */
	QuickFindAction(final FilterController filterController, FilterConditionEditor quickEditor, final Direction direction) {
		super("QuickFindAction." + direction);
		this.filterController = filterController;
		this.filterEditor = quickEditor;
		this.direction =direction ;
	}

	public void executeAction(final boolean reFocusSearchInputField)
	{
		final ASelectableCondition condition = filterEditor.getCondition();
		if(condition == null){
			return;
		}
		final IMapSelection selection = Controller.getCurrentController().getSelection();
		if(selection == null){
			return;
		}
		final NodeModel selected = selection.getSelected();

		final NodeModel next;
		try
		{
			filterEditor.setSearchingBusyCursor();
			next = filterController.findNext(selected, null, direction, condition, selection.getFilter());
		}
		finally
		{
			filterEditor.setSearchingDefaultCursor();
		}

		if(next != null){
			FoundNodes.get(next.getMap()).displayFoundNode(next);
			
			if (reFocusSearchInputField) 
			{
				// this is called by Enter key listener in FilterConditionEditor
				// => we want to re-focus the search term input field so that one can hit enter
				// again to find the next search result!
				filterEditor.focusInputField(false);
			}
		}
	}

	public void actionPerformed(final ActionEvent e) {
		executeAction(false);
	}
}
