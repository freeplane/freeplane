package org.freeplane.plugin.collaboration.client.event.children;

import java.util.ArrayList;
import java.util.List;

import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdated.Side;

public class UpdateEventFactory {
	public ChildrenUpdated createChildrenUpdatedEvent(final NodeModel parent) {
		final List<NodeModel> childNodes = parent.getChildren();
		List<String> childIds = new ArrayList<>(childNodes.size());
		for (NodeModel child : childNodes) {
			if(parent.isRoot())
				childIds.add(Side.of(child).name());
			childIds.add(child.createID());
		}
		return ChildrenUpdated.builder().nodeId(parent.createID()).content(childIds).build();
	}
}
