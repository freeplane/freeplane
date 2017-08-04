package org.freeplane.plugin.collaboration.client;

import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.Update.ContentType;

public class UpdateGenerator {

	public ImmutableUpdate createChildrenUpdate(final NodeModel parent) {
		StringBuilder children = new StringBuilder();
		for (NodeModel child : parent.getChildren())
			children.append(child.getID());
		return ImmutableUpdate.builder().contentType(ContentType.CHILDREN).nodeId(parent.getID()).content(children.toString()).build();
	}

}
