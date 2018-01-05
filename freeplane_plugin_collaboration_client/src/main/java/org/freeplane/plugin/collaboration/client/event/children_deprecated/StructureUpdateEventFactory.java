package org.freeplane.plugin.collaboration.client.event.children_deprecated;

import java.util.ArrayList;
import java.util.List;

import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.children_deprecated.ChildrenUpdated.Child;
import org.freeplane.plugin.collaboration.client.event.children_deprecated.ChildrenUpdated.Side;

public class StructureUpdateEventFactory {
	public ChildrenUpdated createChildrenUpdatedEvent(final NodeModel parent) {
		final List<NodeModel> childNodes = parent.getChildren();
		List<Child> childIds = new ArrayList<>(childNodes.size());
		for (NodeModel child : childNodes) {
			final ImmutableChild.Builder builder = ImmutableChild.builder();
			builder.id(child.createID());
			if(parent.isRoot())
				builder.side(Side.of(child));
			childIds.add(builder.build());
		}
		return ChildrenUpdated.builder().nodeId(parent.createID()).content(childIds).build();
	}
}
