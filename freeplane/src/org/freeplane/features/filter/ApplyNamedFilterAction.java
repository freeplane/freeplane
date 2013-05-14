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

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.filter.condition.ASelectableCondition;

/**
 * @author Dimitry Polivaev
 * 23.03.2013
 */
@SuppressWarnings("serial")
public class ApplyNamedFilterAction extends AFreeplaneAction {
	private final ASelectableCondition condition;
	private final FilterController filterController;

	public ApplyNamedFilterAction(FilterController filterController,  ASelectableCondition condition) {
	    super("ApplyNamedFilterAction." + condition.getUserName(), condition.getUserName(), null);
		this.filterController = filterController;
		this.condition = condition;
    }

	public void actionPerformed(ActionEvent e) {
		filterController.apply(condition);
	}
}
