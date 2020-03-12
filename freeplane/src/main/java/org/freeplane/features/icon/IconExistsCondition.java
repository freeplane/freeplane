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
package org.freeplane.features.icon;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

public class IconExistsCondition extends ASelectableCondition {
	static final String NAME = "icon_exists_condition";

	static ASelectableCondition load(final XMLElement element) {
		return new IconExistsCondition();
	}

	public IconExistsCondition() {
	}

	public boolean checkNode(final NodeModel node) {
		return IconController.getController().getIcons(node).size() > 0;
	}


	@Override
    protected String createDescription() {
        return ConditionFactory.createDescription(TextUtils.getText("filter_icon"), TextUtils.getText(ConditionFactory.FILTER_EXIST),
                null);
    }

	@Override
    protected String getName() {
	    return NAME;
    }
}
