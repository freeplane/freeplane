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
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConjunctConditions;
import org.freeplane.features.filter.condition.DisjunctConditions;
import org.freeplane.features.filter.condition.NoFilteringCondition;

/**
 * @author Dimitry Polivaev
 * Mar 30, 2009
 */
class QuickOrFilterAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private final FilterController filterController;
	private final FilterConditionEditor filterEditor;

	/**
	 * @param filterController
	 * @param quickEditor 
	 */
	QuickOrFilterAction(final FilterController filterController, FilterConditionEditor quickEditor) {
		super("QuickOrFilterAction");
		this.filterController = filterController;
		this.filterEditor = quickEditor;
	}

	public void actionPerformed(final ActionEvent e) {
		final ASelectableCondition condition = filterEditor.getCondition();
		if(condition == null){
			return;
		}
		ASelectableCondition selectedCondition = filterController.getSelectedCondition();
        if (condition.equals(selectedCondition)) {
            return;
		} 
        if(NoFilteringCondition.createCondition().equals(selectedCondition))
            filterController.apply(condition);
        else
            filterController.apply(DisjunctConditions.combine(selectedCondition, condition));
	}
}
