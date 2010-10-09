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
package org.freeplane.features.common.text;

import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.io.xml.TreeXmlWriter;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.filter.condition.CompareConditionAdapter;
import org.freeplane.features.common.filter.condition.ISelectableCondition;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

class NodeTextCompareCondition extends CompareConditionAdapter {
	static final String COMPARATION_RESULT = "COMPARATION_RESULT";
	static final String NAME = "node_compare_condition";
	static final String SUCCEED = "SUCCEED";
	static final String VALUE = "VALUE";
	static final String ITEM = "ITEM";

	static ISelectableCondition load(final XMLElement element) {
		return new NodeTextCompareCondition(
			element.getAttribute(NodeTextCompareCondition.ITEM, NodeTextConditionController.FILTER_NODE), 
			element.getAttribute(NodeTextCompareCondition.VALUE, null), 
			TreeXmlReader.xmlToBoolean(element.getAttribute(CompareConditionAdapter.IGNORE_CASE, null)), 
			Integer.parseInt(element.getAttribute(NodeTextCompareCondition.COMPARATION_RESULT, null)), 
			TreeXmlReader.xmlToBoolean(element.getAttribute(NodeTextCompareCondition.SUCCEED, null)));
	}

	final private int comparationResult;
	final private boolean succeed;
	final private String nodeItem;

	NodeTextCompareCondition(String nodeItem, final String value, final boolean ignoreCase, final int comparationResult,
	                     final boolean succeed) {
		super(value, ignoreCase);
		this.comparationResult = comparationResult;
		this.succeed = succeed;
		this.nodeItem=nodeItem;
	}

	public boolean checkNode(final NodeModel node) {
		final String text = NodeTextConditionController.getItemForComparison(nodeItem, node);
		return text != null && checkText(text);
	}

	private boolean checkText(final String plainTextContent) {
		try {
			return succeed == (compareTo(plainTextContent) == comparationResult);
		}
		catch (final NumberFormatException e) {
			return false;
		}
	}

	@Override
	protected String createDesctiption() {
		final String nodeCondition = TextUtils.getText(nodeItem);
		return super.createDescription(nodeCondition, comparationResult, succeed);
	}

	public void toXml(final XMLElement element) {
		final XMLElement child = new XMLElement();
		child.setName(NodeTextCompareCondition.NAME);
		super.attributesToXml(child);
		child.setAttribute(NodeTextCompareCondition.COMPARATION_RESULT, Integer.toString(comparationResult));
		child.setAttribute(NodeTextCompareCondition.SUCCEED, TreeXmlWriter.BooleanToXml(succeed));
		child.setAttribute(NodeTextCompareCondition.ITEM, nodeItem);
		element.addChild(child);
	}
}
