package org.freeplane.plugin.collaboration.client;

import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.UpdateSpecification.ContentType;

public class UpdateSpecificationGenerator {

	public ChildrenUpdateSpecification createChildrenUpdate(final NodeModel parent) {
		StringBuilder children = new StringBuilder();
		for (NodeModel child : parent.getChildren()) {
			if(children.length() > 0)
				children.append(',');
			children.append(child.getID());
		}
		return ImmutableChildrenUpdateSpecification.builder().contentType(ContentType.CHILDREN).nodeId(parent.getID()).content(children.toString()).build();
	}

}
