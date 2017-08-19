package org.freeplane.plugin.collaboration.client.event.children;

import java.util.ArrayList;
import java.util.List;

import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.ImmutableChildrenUpdated;

public class UpdateEventFactory {

	public ChildrenUpdated createChildrenUpdatedEvent(final NodeModel parent) {
		final List<NodeModel> childNodes = parent.getChildren();
		List<String> childIds = new ArrayList<>(childNodes.size());
		for (NodeModel child : childNodes) {
			childIds.add(child.createID());
		}
		return ImmutableChildrenUpdated.builder().nodeId(parent.createID()).content(childIds).build();
	}

}
