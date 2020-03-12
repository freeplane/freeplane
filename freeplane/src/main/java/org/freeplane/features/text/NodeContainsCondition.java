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

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.StringMatchingStrategy;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.filter.condition.StringConditionAdapter;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

public class NodeContainsCondition extends StringConditionAdapter implements NodeItemRelation {
	static final String IGNORE_CASE_NAME = "node_contains_condition";
	static final String MATCH_CASE_NAME = "match_case_node_contains_condition";
    public static final String VALUE = "VALUE";

	static ASelectableCondition loadIgnoreCase(final XMLElement element) {
		return new NodeContainsCondition(
			element.getAttribute(NodeTextCompareCondition.ITEM, TextController.FILTER_NODE),
			element.getAttribute(NodeContainsCondition.VALUE, null),
			false, Boolean.valueOf(element.getAttribute(MATCH_APPROXIMATELY, null)));
	}

    static ASelectableCondition loadMatchCase(final XMLElement element) {
        return new NodeContainsCondition(
            element.getAttribute(NodeTextCompareCondition.ITEM, TextController.FILTER_NODE),
            element.getAttribute(NodeContainsCondition.VALUE, null),
            true, Boolean.valueOf(element.getAttribute(MATCH_APPROXIMATELY, null)));
    }
	final private String value;
	final private String nodeItem;
	final StringMatchingStrategy stringMatchingStrategy;

	public NodeContainsCondition(String nodeItem, final String value, boolean matchCase, final boolean matchApproximately) {
		super(matchCase, matchApproximately);
		this.value = value;
		//this.valueLowerCase = value.toLowerCase();
		this.nodeItem = nodeItem;
		stringMatchingStrategy = matchApproximately ? StringMatchingStrategy.DEFAULT_APPROXIMATE_STRING_MATCHING_STRATEGY :
			StringMatchingStrategy.EXACT_STRING_MATCHING_STRATEGY;
	}

	public boolean checkNode(final NodeModel node) {
		final Object content[] = NodeTextConditionController.getItemsForComparison(nodeItem, node);
		return checkText(content);
	}

	private boolean checkText(Object content[]) {
		for(Object o : content){
			if(checkText(o))
				return true;
		}
		return false;
	}

	private boolean checkText(final Object o) {
		return o != null && stringMatchingStrategy.matches(normalizedValue(), normalize(o), true);
	}

	@Override
	protected String createDescription() {
		final String nodeCondition = TextUtils.getText(nodeItem);
		final String simpleCondition = TextUtils.getText(ConditionFactory.FILTER_CONTAINS);
		return ConditionFactory.createDescription(nodeCondition, simpleCondition, value, matchCase, matchApproximately);
	}

	@Override
	public void fillXML(final XMLElement child) {
		super.fillXML(child);
		child.setAttribute(NodeContainsCondition.VALUE, value);
		child.setAttribute(NodeTextCompareCondition.ITEM, nodeItem);
	}

	@Override
    protected String getName() {
	    return matchCase ? MATCH_CASE_NAME : IGNORE_CASE_NAME;
    }

	public String getNodeItem() {
		return nodeItem;
	}

    @Override
    protected Object conditionValue() {
        return value;
    }

}
