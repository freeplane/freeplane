/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
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
package org.freeplane.features.filter.condition;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;

public class NoFilteringCondition extends ASelectableCondition {
	private static final String NAME = "no_filtering_condition";
	private static NoFilteringCondition condition;
	private static String description;

	public static ASelectableCondition createCondition() {
		if (NoFilteringCondition.condition == null) {
			NoFilteringCondition.condition = new NoFilteringCondition();
		}
		return NoFilteringCondition.condition;
	}

	private NoFilteringCondition() {
		super();
	}

	public boolean checkNode(final NodeModel node) {
		return true;
	}

	@Override
    protected String createDescription() {
		if (NoFilteringCondition.description == null) {
			NoFilteringCondition.description = TextUtils.getText("filter_no_filtering");
		}
		return NoFilteringCondition.description;
    }

	@Override
    protected String getName() {
	    return NAME;
    }
	
	@Override
	public boolean canBePersisted() {
	    return false;
	}
}
