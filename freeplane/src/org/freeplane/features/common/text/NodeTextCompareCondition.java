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
import org.freeplane.core.util.TextUtils;
import org.freeplane.core.util.TypeReference;
import org.freeplane.features.common.filter.condition.CompareConditionAdapter;
import org.freeplane.features.common.filter.condition.ASelectableCondition;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.text.NodeTextConditionController.ItemForComparison;
import org.freeplane.n3.nanoxml.XMLElement;

public class NodeTextCompareCondition extends CompareConditionAdapter {
	static final String COMPARATION_RESULT = "COMPARATION_RESULT";
	static final String NAME = "node_compare_condition";
	static final String SUCCEED = "SUCCEED";
	static final String VALUE = "VALUE";
	static final String ITEM = "ITEM";

	static ASelectableCondition load(final XMLElement element) {
		final String item = element.getAttribute(NodeTextCompareCondition.ITEM, TextController.FILTER_NODE);
		final String valueString = element.getAttribute(NodeTextCompareCondition.VALUE, null);
		final Object value;
		if(valueString != null)
			value = valueString;
		else{
			final String object = element.getAttribute(NodeTextCompareCondition.OBJECT, null);
			value = TypeReference.create(object);
		}
		final boolean matchCase = TreeXmlReader.xmlToBoolean(element.getAttribute(CompareConditionAdapter.MATCH_CASE, null));
		final int compResult = Integer.parseInt(element.getAttribute(NodeTextCompareCondition.COMPARATION_RESULT, null));
		final boolean succeed = TreeXmlReader.xmlToBoolean(element.getAttribute(NodeTextCompareCondition.SUCCEED, null));
		return new NodeTextCompareCondition(
			item, 
			value, 
			matchCase, 
			compResult, 
			succeed);
	}

	final private int comparationResult;
	final private boolean succeed;
	final private String nodeItem;

	NodeTextCompareCondition(String nodeItem, final Object value, final boolean matchCase, final int comparationResult,
	                     final boolean succeed) {
		super(value, matchCase);
		this.comparationResult = comparationResult;
		this.succeed = succeed;
		this.nodeItem=nodeItem;
	}

	public boolean checkNode(final NodeModel node) {
		final ItemForComparison item = NodeTextConditionController.getItemForComparison(nodeItem, node);
		return item != null && checkText(item.content, item.text);
	}

	private boolean checkText(Object content, final String plainTextContent) {
		try {
			compareTo(content, plainTextContent);
			return isComparisonOK() &&  succeed == (getComparisonResult() == comparationResult);
		}
		catch (final NumberFormatException e) {
			return false;
		}
	}

	@Override
	protected String createDescription() {
		final String nodeCondition = TextUtils.getText(nodeItem);
		return super.createDescription(nodeCondition, comparationResult, succeed);
	}

	public void fillXML(final XMLElement child) {
		super.fillXML(child);
		child.setAttribute(NodeTextCompareCondition.COMPARATION_RESULT, Integer.toString(comparationResult));
		child.setAttribute(NodeTextCompareCondition.SUCCEED, TreeXmlWriter.BooleanToXml(succeed));
		child.setAttribute(NodeTextCompareCondition.ITEM, nodeItem);
	}

	@Override
    protected String getName() {
	    return NAME;
    }
}
