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
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

public class NodeContainsCondition extends ASelectableCondition implements NodeItemRelation {
	static final String NAME = "node_contains_condition";
	static final String VALUE = "VALUE";
	static final String MATCH_APPROXIMATELY = "MATCH_APPROXIMATELY";

	static ASelectableCondition load(final XMLElement element) {
		return new NodeContainsCondition(
			element.getAttribute(NodeTextCompareCondition.ITEM, TextController.FILTER_NODE), 
			element.getAttribute(NodeContainsCondition.VALUE, null),
			Boolean.valueOf(element.getAttribute(NodeContainsCondition.MATCH_APPROXIMATELY, null)));
	}

	final private String value;
	final private String nodeItem;
	//final private String valueLowerCase;
	final private boolean matchApproximately;
	final StringMatchingStrategy stringMatchingStrategy;

	public NodeContainsCondition(String nodeItem, final String value, final boolean matchApproximately) {
		super();
		this.value = value;
		//this.valueLowerCase = value.toLowerCase();
		this.nodeItem = nodeItem;
		this.matchApproximately = matchApproximately; 
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
		//return o != null && o.toString().toLowerCase().indexOf(valueLowerCase) > -1;
		return o != null && stringMatchingStrategy.matches(value, o.toString(), true, false);
	}

	@Override
	protected String createDescription() {
		final String nodeCondition = TextUtils.getText(nodeItem);
		final String simpleCondition = TextUtils.getText(ConditionFactory.FILTER_CONTAINS);
		return ConditionFactory.createDescription(nodeCondition, simpleCondition, value, false, matchApproximately);
	}

	@Override
	public void fillXML(final XMLElement child) {
		super.fillXML(child);
		child.setAttribute(NodeContainsCondition.VALUE, value);
		child.setAttribute(NodeTextCompareCondition.ITEM, nodeItem);
		child.setAttribute(NodeContainsCondition.MATCH_APPROXIMATELY, Boolean.toString(matchApproximately));
	}

	@Override
    protected String getName() {
	    return NAME;
    }
	
	public String getNodeItem() {
		return nodeItem;
	}

}
