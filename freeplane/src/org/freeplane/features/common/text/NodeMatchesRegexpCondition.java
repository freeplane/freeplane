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

import org.freeplane.core.filter.condition.CompareConditionAdapter;
import org.freeplane.core.filter.condition.ConditionFactory;
import org.freeplane.core.filter.condition.ISelectableCondition;
import org.freeplane.core.filter.condition.NodeCondition;
import org.freeplane.core.io.xml.TreeXmlWriter;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.util.HtmlTools;
import org.freeplane.n3.nanoxml.XMLElement;

public class NodeMatchesRegexpCondition extends NodeCondition {
	static final String NAME = "node_matches_regexp";
	static final String SEARCH_PATTERN = "SEARCH_PATTERN";

	static ISelectableCondition load(final XMLElement element) {
		final Boolean ignoreCase = Boolean.valueOf(element
		    .getAttribute(NodeCompareCondition.IGNORE_CASE, "false"));
		final String searchPattern = element.getAttribute(SEARCH_PATTERN, null);
		return new NodeMatchesRegexpCondition(searchPattern, ignoreCase);
	}

	private final Pattern searchPattern;

	public NodeMatchesRegexpCondition(String searchPattern) {
		this(searchPattern, false);
	}

	public NodeMatchesRegexpCondition(String searchPattern, boolean ignoreCase) {
		super();
		int flags = 0;
		if (ignoreCase)
			flags |= Pattern.CASE_INSENSITIVE;
		this.searchPattern = Pattern.compile(searchPattern, flags);
	}

	public boolean checkNode(final NodeModel node) {
		String text = node.getPlainTextContent();
		return checkText(text);
	}

	boolean checkText(String text) {
		return searchPattern.matcher(text).matches();
	}

	@Override
	protected String createDesctiption() {
		final String nodeCondition = ResourceBundles.getText(NodeConditionController.FILTER_NODE);
		final String simpleCondition = ResourceBundles.getText(ConditionFactory.FILTER_REGEXP);
		return ConditionFactory.createDescription(nodeCondition, simpleCondition, searchPattern.pattern(),
		    isIgnoreCase());
	}

	private String getText(final NodeModel node) {
		return node.getText();
	}

	public void toXml(final XMLElement element) {
		final XMLElement child = new XMLElement();
		child.setName(NAME);
		super.attributesToXml(child);
		child.setAttribute(SEARCH_PATTERN, searchPattern.pattern());
		child.setAttribute(CompareConditionAdapter.IGNORE_CASE, TreeXmlWriter.BooleanToXml(isIgnoreCase()));
		element.addChild(child);
	}

	private boolean isIgnoreCase() {
		return (searchPattern.flags() & Pattern.CASE_INSENSITIVE) != 0;
	}
}
