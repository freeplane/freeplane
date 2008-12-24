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
package org.freeplane.map.attribute.filter;

import org.freeplane.controller.Controller;
import org.freeplane.controller.filter.condition.ConditionFactory;
import org.freeplane.controller.filter.condition.ICondition;
import org.freeplane.io.xml.n3.nanoxml.XMLElement;
import org.freeplane.map.attribute.IAttributeTableModel;
import org.freeplane.map.text.filter.NodeCondition;
import org.freeplane.map.tree.NodeModel;

/**
 * @author Dimitry Polivaev
 */
public class AttributeExistsCondition extends NodeCondition {
	static final String ATTRIBUTE = "attribute";
	static final String NAME = "attribute_exists_condition";

	static ICondition load(final XMLElement element) {
		return new AttributeExistsCondition(element.getAttribute(
		    AttributeExistsCondition.ATTRIBUTE, null));
	}

	final private String attribute;

	/**
	 */
	public AttributeExistsCondition(final String attribute) {
		super();
		this.attribute = attribute;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freemind.controller.filter.condition.Condition#checkNode(freemind.modes
	 * .MindMapNode)
	 */
	public boolean checkNode(final NodeModel node) {
		final IAttributeTableModel attributes = node.getAttributes();
		for (int i = 0; i < attributes.getRowCount(); i++) {
			if (attributes.getValueAt(i, 0).equals(attribute)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected String createDesctiption() {
		final String simpleCondition = Controller.getText(ConditionFactory.FILTER_EXIST);
		return ConditionFactory.createDescription(attribute, simpleCondition, null, false);
	}

	public void toXml(final XMLElement element) {
		final XMLElement child = new XMLElement();
		child.setName(AttributeExistsCondition.NAME);
		super.attributesToXml(child);
		child.setAttribute(AttributeExistsCondition.ATTRIBUTE, attribute);
		element.addChild(child);
	}
}
