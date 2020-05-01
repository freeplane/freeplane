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
package org.freeplane.features.attribute;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 */
public class AttributeNotExistsCondition extends ASelectableCondition {
	static final String ATTRIBUTE = "ATTRIBUTE";
	static final String NAME = "attribute_not_exists_condition";

	static ASelectableCondition load(final XMLElement element) {
		return new AttributeNotExistsCondition(
			AttributeConditionController.toAttributeObject(element.getAttribute(AttributeNotExistsCondition.ATTRIBUTE, null))
		);
	}


	final private Object attribute;

	/**
	 */
	public AttributeNotExistsCondition(final Object attribute) {
		super();
		this.attribute = attribute;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.filter.condition.Condition#checkNode(freeplane.modes
	 * .MindMapNode)
	 */
	public boolean checkNode(final NodeModel node) {
		final IAttributeTableModel attributes = NodeAttributeTableModel.getModel(node);
		for (int i = 0; i < attributes.getRowCount(); i++) {
			if (attributes.getValueAt(i, 0).equals(attribute)) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected String createDescription() {
		final String simpleCondition = TextUtils.getText(ConditionFactory.FILTER_DOES_NOT_EXIST);
		return ConditionFactory.createDescription(attribute.toString(), simpleCondition, null);
	}

	public void fillXML(final XMLElement child) {
		super.fillXML(child);
		if (attribute instanceof String) child.setAttribute(ATTRIBUTE, (String) attribute);
	}

	@Override
    protected String getName() {
	    return NAME;
    }
}
