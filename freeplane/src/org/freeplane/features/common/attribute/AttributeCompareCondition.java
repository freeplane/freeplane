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
package org.freeplane.features.common.attribute;

import org.freeplane.core.filter.condition.CompareConditionAdapter;
import org.freeplane.core.filter.condition.ISelectableCondition;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.io.xml.TreeXmlWriter;
import org.freeplane.core.model.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 */
public class AttributeCompareCondition extends CompareConditionAdapter {
	static final String ATTRIBUTE = "ATTRIBUTE";
	static final String COMPARATION_RESULT = "COMPARATION_RESULT";
	static final String NAME = "attribute_compare_condition";
	static final String SUCCEED = "SUCCEED";

	static ISelectableCondition load(final XMLElement element) {
		return new AttributeCompareCondition(element.getAttribute(AttributeCompareCondition.ATTRIBUTE, null), element
		    .getAttribute(CompareConditionAdapter.VALUE, null), TreeXmlReader.xmlToBoolean(element.getAttribute(
		    CompareConditionAdapter.IGNORE_CASE, null)), Integer.parseInt(element.getAttribute(
		    AttributeCompareCondition.COMPARATION_RESULT, null)), TreeXmlReader.xmlToBoolean(element.getAttribute(
		    AttributeCompareCondition.SUCCEED, null)));
	}

	final private String attribute;
	final private int comparationResult;
	final private boolean succeed;

	/**
	 */
	public AttributeCompareCondition(final String attribute, final String value, final boolean ignoreCase,
	                                 final int comparationResult, final boolean succeed) {
		super(value, ignoreCase);
		this.attribute = attribute;
		this.comparationResult = comparationResult;
		this.succeed = succeed;
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
			try {
				if (attributes.getValueAt(i, 0).equals(attribute)
				        && succeed == (compareTo(attributes.getValueAt(i, 1).toString()) == comparationResult)) {
					return true;
				}
			}
			catch (final NumberFormatException fne) {
			}
		}
		return false;
	}

	@Override
	protected String createDesctiption() {
		return super.createDescription(attribute, comparationResult, succeed);
	}

	public void toXml(final XMLElement element) {
		final XMLElement child = new XMLElement();
		child.setName(AttributeCompareCondition.NAME);
		super.attributesToXml(child);
		child.setAttribute(AttributeCompareCondition.ATTRIBUTE, attribute);
		child.setAttribute(AttributeCompareCondition.COMPARATION_RESULT, Integer.toString(comparationResult));
		child.setAttribute(AttributeCompareCondition.SUCCEED, TreeXmlWriter.BooleanToXml(succeed));
		element.addChild(child);
	}
}
