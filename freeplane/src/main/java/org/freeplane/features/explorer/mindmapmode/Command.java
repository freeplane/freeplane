package org.freeplane.features.explorer.mindmapmode;

import java.util.Collection;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.TextController;

class Command {
	private static final String REST_CHARACTERS = "...";
	private final ExploringStep operator;
	private final String searchedString;
	private final TextController textController;
	private NodeModel start;
	public Command(TextController textController, NodeModel start, ExploringStep operator, String searchedString) {
		super();
		this.textController = textController;
		this.start = start;
		operator.assertValidString(searchedString);
		this.operator = operator;
		this.searchedString = searchedString;
	}

	public NodeModel getSingleNode(NodeModel start) {
		final NodeMatcher nodeMatcher = createMatcher();
		return operator.getSingleNode(start, nodeMatcher);
	}

	public Collection<? extends NodeModel> getAllNodes(NodeModel from) {
		final NodeMatcher nodeMatcher = createMatcher();
		return operator.getAllNodes(start, nodeMatcher);
	}

	private NodeMatcher createMatcher() {
		boolean matchStart = searchedString.endsWith(REST_CHARACTERS);
		String matchedString = matchStart ? searchedString.substring(searchedString.length() - REST_CHARACTERS.length()) : searchedString;
		final NodeMatcher nodeMatcher = new NodeMatcher(textController, matchedString, matchStart);
		return nodeMatcher;
	}

}