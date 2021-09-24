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

import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 */
public class ParentConditionDecorator extends DecoratedCondition implements ICombinedCondition{
	static final String DESCRIPTION = "filter_for_parent";
    static final String NAME = "parent_condition";

	static ASelectableCondition load(final ConditionFactory conditionFactory, final XMLElement element) {
		final ASelectableCondition cond = DecoratedCondition.loadOriginalCondition(conditionFactory, element);
		return cond == null ? null : new ParentConditionDecorator(cond);
	}

	public ParentConditionDecorator(final ASelectableCondition originalCondition) {
		super(originalCondition, NAME, DESCRIPTION);
	}

	public boolean checkNode(final NodeModel node) {
		NodeModel parentNode = node.getParentNode();
        return parentNode != null && originalCondition.checkNode(parentNode);
	}

    @Override
    public boolean checksParent() {
        return true;
    }
	
	

}
