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

import java.util.HashMap;
import java.util.HashSet;

import javax.swing.DefaultComboBoxModel;

import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.UIBuilder;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.mode.ModeController;

/**
 * @author Dimitry Polivaev
 * 23.03.2013
 */
public class FilterMenuBuilder implements IMenuContributor {
	private static final String MENU_USER_DEFINED_FILTERS = "menu_user_defined_filters";
	private final FilterController filterController;
	FilterMenuBuilder(FilterController filtercontroller){
		this.filterController = filtercontroller;
	}
	public void updateMenus(ModeController modeController, MenuBuilder builder) {
		if(builder.get(MENU_USER_DEFINED_FILTERS) == null)
			return;
		builder.removeChildElements(MENU_USER_DEFINED_FILTERS);
		final DefaultComboBoxModel filterConditions = filterController.getFilterConditions();
		final HashSet<String> usedNames = new HashSet<String>();
		for(int i = 0; i < filterConditions.getSize(); i++){
			final ASelectableCondition condition = (ASelectableCondition) filterConditions.getElementAt(i);
			final String conditionName = condition.getUserName();
			if(conditionName != null && usedNames.add(conditionName)){
				final ApplyNamedFilterAction action = new ApplyNamedFilterAction(filterController, condition);
				builder.addAction(MENU_USER_DEFINED_FILTERS, action,UIBuilder.AS_CHILD );
			}
		}
	}
}
