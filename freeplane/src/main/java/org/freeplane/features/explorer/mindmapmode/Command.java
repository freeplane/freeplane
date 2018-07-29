package org.freeplane.features.explorer.mindmapmode;

import java.util.Collection;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.TextController;

class Command {
	private static final String REST_CHARACTERS = "...";
	private final ExploringStep operator;
	private final String searchedString;
	private final TextController textController;
	private final AccessedNodes accessedNodes;
	public Command(TextController textController, ExploringStep operator, String searchedString, AccessedNodes accessedNodes) {
		super();
		this.textController = textController;
		this.accessedNodes = accessedNodes;
		operator.assertValidString(searchedString);
		this.operator = operator;
		this.searchedString = searchedString;
	}

	public NodeModel getSingleNode(NodeModel start) {
		final NodeMatcher nodeMatcher = createMatcher();
		return operator.getSingleNode(start, nodeMatcher, accessedNodes);
	}

	public Collection<? extends NodeModel> getAllNodes(NodeModel start) {
		final NodeMatcher nodeMatcher = createMatcher();
		return operator.getAllNodes(start, nodeMatcher, accessedNodes);
	}

	private NodeMatcher createMatcher() {
		boolean matchStart = searchedString.endsWith(REST_CHARACTERS);
		String matchedString = matchStart ? searchedString.substring(0, searchedString.length() - REST_CHARACTERS.length()) : searchedString;
		final NodeMatcher nodeMatcher = new NodeMatcher(textController, matchedString, matchStart);
		return nodeMatcher;
	}

}