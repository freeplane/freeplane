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
package org.freeplane.features.text;

import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.io.xml.TreeXmlWriter;
import org.freeplane.core.util.TextUtils;
import org.freeplane.core.util.TypeReference;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.CompareConditionAdapter;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

public class NodeTextCompareCondition extends CompareConditionAdapter implements NodeItemRelation {
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
		final boolean matchApproximately = TreeXmlReader.xmlToBoolean(element.getAttribute(NodeTextCompareCondition.MATCH_APPROXIMATELY, null));
		return new NodeTextCompareCondition(
			item, 
			value, 
			matchCase, 
			compResult, 
			succeed,
			matchApproximately);
	}

	final private int comparationResult;
	final private boolean succeed;
	final private String nodeItem;

	NodeTextCompareCondition(String nodeItem, final Object value, final boolean matchCase, final int comparationResult,
	                     final boolean succeed, final boolean matchApproximately) {
		super(value, matchCase, matchApproximately);
		this.comparationResult = comparationResult;
		this.succeed = succeed;
		this.nodeItem=nodeItem;
	}
	
	public boolean isEqualityCondition()
	{
		return comparationResult == 0;
	}

	public boolean checkNode(final NodeModel node) {
		final Object content[] = NodeTextConditionController.getItemsForComparison(nodeItem, node);
		return content != null && checkContents(content);
	}

	private boolean checkContents(Object content[]) {
		for(Object o : content){
			if(o != null && checkContent(o))
				return true;
		}
		return false;
	}
	
	private boolean checkContent(final Object content) {
		try {
			compareTo(content);
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
	
	public String getNodeItem() {
		return nodeItem;
	}

}
