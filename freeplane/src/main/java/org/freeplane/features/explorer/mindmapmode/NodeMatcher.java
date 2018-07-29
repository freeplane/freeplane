package org.freeplane.features.explorer.mindmapmode;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.TextController;

class NodeMatcher {
	private final String matchedString;
	private final boolean matchStart;
	private final TextController textController;

	public NodeMatcher(TextController textController, String matchedString, boolean matchStart) {
		this.textController = textController;
		this.matchedString = matchedString;
		this.matchStart = matchStart;
	}

	public boolean matches(NodeModel node) {
		return matches(node.getExtension(NodeAlias.class))
				|| matches(textController.getPlainTransformedTextWithoutNodeNumber(node));
	}

	private boolean matches(NodeAlias alias) {
		return alias != null && matchedString.equals(alias.value);
	}

	private boolean matches(final String text) {
		return matchStart && text.startsWith(matchedString) || text.equals(matchedString);
	}
}