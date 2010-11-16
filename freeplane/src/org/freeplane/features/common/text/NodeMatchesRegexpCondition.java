/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 Volker Boerchers
 *
 *  This file author is Volker Boerchers
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
 *  along with this program.  If not, see &lt;http://www.gnu.org/licenses/&gt;.
 */
package org.freeplane.features.common.text;

import java.util.regex.Pattern;

import org.freeplane.core.io.xml.TreeXmlWriter;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.filter.condition.CompareConditionAdapter;
import org.freeplane.features.common.filter.condition.ConditionFactory;
import org.freeplane.features.common.filter.condition.ASelectableCondition;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

public class NodeMatchesRegexpCondition extends ASelectableCondition {
	static final String NAME = "node_matches_regexp";
	static final String SEARCH_PATTERN = "SEARCH_PATTERN";

	static ASelectableCondition load(final XMLElement element) {
		final Boolean ignoreCase = Boolean.valueOf(element.getAttribute(NodeTextCompareCondition.IGNORE_CASE, "false"));
		final String searchPattern = element.getAttribute(SEARCH_PATTERN, null);
		final String nodeItem = element.getAttribute(NodeTextCompareCondition.ITEM, TextController.FILTER_NODE);
		return new NodeMatchesRegexpCondition(nodeItem, searchPattern, ignoreCase);
	}

	private final Pattern searchPattern;
	final private String nodeItem;

	public NodeMatchesRegexpCondition(String nodeItem, final String searchPattern) {
		this(nodeItem, searchPattern, false);
	}

	public NodeMatchesRegexpCondition(String nodeItem, final String searchPattern, final boolean ignoreCase) {
		super();
		int flags = Pattern.DOTALL;
		if (ignoreCase) {
			flags |= Pattern.CASE_INSENSITIVE;
		}
		this.searchPattern = Pattern.compile(searchPattern, flags);
		this.nodeItem=nodeItem;
	}

	public boolean checkNode(final NodeModel node) {
		final String text = NodeTextConditionController.getItemForComparison(nodeItem, node);
		return text != null && checkText(text);
	}

	boolean checkText(final String text) {
		return searchPattern.matcher(text).matches();
	}

	@Override
	protected String createDesctiption() {
		final String nodeCondition = TextUtils.getText(nodeItem);
		final String simpleCondition = TextUtils.getText(ConditionFactory.FILTER_REGEXP);
		return ConditionFactory.createDescription(nodeCondition, simpleCondition, searchPattern.pattern(),
		    isIgnoreCase());
	}

	public void fillXML(final XMLElement child) {
		super.fillXML(child);
		child.setAttribute(SEARCH_PATTERN, searchPattern.pattern());
		child.setAttribute(CompareConditionAdapter.IGNORE_CASE, TreeXmlWriter.BooleanToXml(isIgnoreCase()));
		child.setAttribute(NodeTextCompareCondition.ITEM, nodeItem);
	}

	private boolean isIgnoreCase() {
		return (searchPattern.flags() & Pattern.CASE_INSENSITIVE) != 0;
	}

	@Override
    protected String getName() {
	    return NAME;
    }
}
