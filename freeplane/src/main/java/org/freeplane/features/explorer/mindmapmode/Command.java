package org.freeplane.features.explorer.mindmapmode;

import java.util.Collection;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.TextController;

class Command {
	private final ExploringStep operator;
	private final TextController textController;
	private final AccessedNodes accessedNodes;
	private final String searchedString;
	public Command(TextController textController, ExploringStep operator, String searchedString, AccessedNodes accessedNodes) {
		super();
		this.searchedString = searchedString;
		operator.assertValidString(searchedString);
		this.textController = textController;
		this.accessedNodes = accessedNodes;
		this.operator = operator;
	}

	public Collection<? extends NodeModel> getNodes(NodeModel start) {
		final NodeMatcher nodeMatcher = createMatcher();
		return operator.getAllNodes(start, nodeMatcher, accessedNodes);
	}

	private NodeMatcher createMatcher() {
		return new NodeMatcher(textController, searchedString);
	}

	@Override
	public String toString() {
		return "Command [operator=" + operator + ", searchedString=" + searchedString + "]";
	}



}