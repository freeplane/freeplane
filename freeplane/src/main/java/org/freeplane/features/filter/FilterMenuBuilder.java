/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2013 Dimitry
 *
 *  This file author is Dimitry
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

import java.util.HashSet;

import javax.swing.DefaultComboBoxModel;

import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 * 23.03.2013
 */
public class FilterMenuBuilder implements EntryVisitor {
	private final FilterController filterController;
	private final Controller controller;
	FilterMenuBuilder(Controller controller, FilterController filterController){
		this.controller = controller;
		this.filterController = filterController;
	}
	@Override
	public void visit(Entry target) {
		final DefaultComboBoxModel filterConditions = filterController.getFilterConditions();
		final HashSet<String> usedNames = new HashSet<String>();
		for(int i = 0; i < filterConditions.getSize(); i++){
			final ASelectableCondition condition = (ASelectableCondition) filterConditions.getElementAt(i);
			final String conditionName = condition.getUserName();
			if(conditionName != null && usedNames.add(conditionName)){
				final ApplyNamedFilterAction action = new ApplyNamedFilterAction(filterController, condition);
				controller.addActionIfNotAlreadySet(action);
				new EntryAccessor().addChildAction(target, action);
			}
		}

	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return true;
	}

	public void updateMenus() {
	    final IUserInputListenerFactory userInputListenerFactory = Controller.getCurrentModeController().getUserInputListenerFactory();
		userInputListenerFactory.rebuildMenus("filterConditions");
    }
}
