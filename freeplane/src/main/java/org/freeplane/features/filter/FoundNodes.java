package org.freeplane.features.filter;

import java.util.LinkedList;
import java.util.ListIterator;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

class FoundNodes implements IExtension {
	String rootID;
	final LinkedList<String> nodesUnfoldedByDisplay = new LinkedList<String>();
	ASelectableCondition condition;

	static FoundNodes get(final MapModel map) {
		if (map == null) {
			return null;
		}
		FoundNodes nodes = map.getExtension(FoundNodes.class);
		if (nodes == null) {
			nodes = new FoundNodes();
			map.addExtension(nodes);
		}
		return nodes;
	}

	/**
	 * Display a node in the display (used by find and the goto action by arrow
	 * link actions).
	 */
	void displayFoundNode(final NodeModel node) {
		final MapModel map = node.getMap();
		final LinkedList<String> nodesUnfoldedByDisplay = new LinkedList<String>();
		NodeModel nodeOnPath = null;
		for (nodeOnPath = node; nodeOnPath != null && !this.nodesUnfoldedByDisplay.contains(nodeOnPath.createID()); nodeOnPath = nodeOnPath
		    .getParentNode()) {
			if (Controller.getCurrentModeController().getMapController().isFolded(nodeOnPath)) {
				nodesUnfoldedByDisplay.add(nodeOnPath.createID());
			}
		}
		final ListIterator<String> oldPathIterator = this.nodesUnfoldedByDisplay
		    .listIterator(this.nodesUnfoldedByDisplay.size());
		while (oldPathIterator.hasPrevious()) {
			final String oldPathNodeID = oldPathIterator.previous();
			final NodeModel oldPathNode = map.getNodeForID_(oldPathNodeID);
			if (oldPathNode != null && oldPathNode.equals(nodeOnPath)) {
				break;
			}
			oldPathIterator.remove();
			if (oldPathNode != null) {
				Controller.getCurrentModeController().getMapController().fold(oldPathNode);
			}
		}
		this.nodesUnfoldedByDisplay.addAll(nodesUnfoldedByDisplay);
		Controller.getCurrentModeController().getMapController().select(node);
	}

}