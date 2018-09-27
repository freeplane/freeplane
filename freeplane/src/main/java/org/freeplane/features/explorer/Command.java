package org.freeplane.features.explorer;

import org.freeplane.features.map.NodeModel;

import java.util.Collection;

class Command {
	private final ExploringStep operator;
	private final AccessedNodes accessedNodes;
	private final String searchedString;
	public Command(ExploringStep operator, String searchedString, AccessedNodes accessedNodes) {
		super();
		this.searchedString = searchedString;
		operator.assertValidString(searchedString);
		this.accessedNodes = accessedNodes;
		this.operator = operator;
	}

	public Collection<? extends NodeModel> getNodes(NodeModel start) {
		final NodeMatcher nodeMatcher = createMatcher();
		return operator.getAllNodes(start, nodeMatcher, accessedNodes);
	}

	private NodeMatcher createMatcher() {
		return new NodeMatcher(searchedString);
	}

	@Override
	public String toString() {
		return "Command [operator=" + operator + ", searchedString=" + searchedString + "]";
	}



}