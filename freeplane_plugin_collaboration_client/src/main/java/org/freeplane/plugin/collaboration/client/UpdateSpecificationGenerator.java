package org.freeplane.plugin.collaboration.client;

import java.util.ArrayList;
import java.util.List;

import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.UpdateSpecification.ContentType;

public class UpdateSpecificationGenerator {

	public ChildrenUpdateSpecification createChildrenUpdate(final NodeModel parent) {
		final List<NodeModel> childNodes = parent.getChildren();
		List<String> childIds = new ArrayList<>(childNodes.size());
		for (NodeModel child : childNodes) {
			childIds.add(child.getID());
		}
		return ImmutableChildrenUpdateSpecification.builder().contentType(ContentType.CHILDREN).nodeId(parent.getID()).content(childIds).build();
	}

}
