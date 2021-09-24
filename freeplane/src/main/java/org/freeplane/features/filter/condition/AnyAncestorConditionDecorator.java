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
public class AnyAncestorConditionDecorator extends DecoratedCondition implements ICombinedCondition{
	static final String DESCRIPTION = "filter_for_any_ancestor";
    static final String NAME = "any_ancestor_condition";

	static ASelectableCondition load(final ConditionFactory conditionFactory, final XMLElement element) {
		final ASelectableCondition cond = DecoratedCondition.loadOriginalCondition(conditionFactory, element);
		return cond == null ? null : new AnyAncestorConditionDecorator(cond);
	}

	public AnyAncestorConditionDecorator(final ASelectableCondition originalCondition) {
		super(originalCondition, NAME, DESCRIPTION);
	}

	public boolean checkNode(final NodeModel node) {
        NodeModel parentNode = node.getParentNode();
        if(parentNode == null)
            return false;
        if (originalCondition.checkNode(parentNode))
            return true;
        return checkNode(parentNode);
	}

    @Override
    public boolean checksAncestors() {
        return true;
    }
	
	

}
