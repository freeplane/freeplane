package org.freeplane.plugin.collaboration.client.event.children;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.MapUpdated;
import org.freeplane.plugin.collaboration.client.event.children.ImmutableSpecialNodeTypeUpdated.Builder;
import org.freeplane.plugin.collaboration.client.event.children.SpecialNodeTypeUpdated.SpecialNodeType;

public class UpdateEventFactory {

	public ChildrenUpdated createChildrenUpdatedEvent(final NodeModel parent) {
		final List<NodeModel> childNodes = parent.getChildren();
		List<String> childIds = new ArrayList<>(childNodes.size());
		for (NodeModel child : childNodes) {
			childIds.add(child.createID());
		}
		return ImmutableChildrenUpdated.builder().nodeId(parent.createID()).content(childIds).build();
	}

	public Optional<MapUpdated> createSpecialNodeTypeUpdatedEvent(NodeModel node) {
		final Optional<SpecialNodeType> type = SpecialNodeType.of(node);
		return type.map(t -> createEvent(node, t).build());
	}

	private Builder createEvent(NodeModel node, SpecialNodeType type) {
		return ImmutableSpecialNodeTypeUpdated.builder().nodeId(node.createID()).content(type);
	}

}
