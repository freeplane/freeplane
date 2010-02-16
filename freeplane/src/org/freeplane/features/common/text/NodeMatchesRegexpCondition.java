/**
 * 
 */
package org.freeplane.features.common.text;

import java.util.regex.Pattern;

import org.freeplane.core.filter.condition.ConditionFactory;
import org.freeplane.core.filter.condition.ICondition;
import org.freeplane.core.filter.condition.NodeCondition;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.util.HtmlTools;
import org.freeplane.n3.nanoxml.XMLElement;

public class NodeMatchesRegexpCondition extends NodeCondition {
	static final String NAME = "node_matches_regexp";
	static final String SEARCH_PATTERN = "SEARCH_PATTERN";

	static ICondition load(final XMLElement element) {
		return new NodeMatchesRegexpCondition(element.getAttribute(SEARCH_PATTERN, null));
	}

	private final Pattern searchPattern;

	public NodeMatchesRegexpCondition(String searchPattern) {
		super();
		this.searchPattern = Pattern.compile(searchPattern);
	}

	public boolean checkNode(final NodeModel node) {
		final String text = getText(node);
		return checkText(text) || HtmlTools.isHtmlNode(text) && checkText(HtmlTools.htmlToPlain(text));
	}

	boolean checkText(String text) {
		return searchPattern.matcher(text).matches();
	}

	@Override
	protected String createDesctiption() {
		final String nodeCondition = ResourceBundles.getText(NodeConditionController.FILTER_NODE);
		final String simpleCondition = ResourceBundles.getText(ConditionFactory.FILTER_REGEXP);
		return ConditionFactory.createDescription(nodeCondition, simpleCondition, searchPattern.pattern(), false);
	}

	private String getText(final NodeModel node) {
		return node.getText();
	}

	public void toXml(final XMLElement element) {
		final XMLElement child = new XMLElement();
		child.setName(NAME);
		super.attributesToXml(child);
		child.setAttribute(SEARCH_PATTERN, searchPattern.pattern());
		element.addChild(child);
	}
}