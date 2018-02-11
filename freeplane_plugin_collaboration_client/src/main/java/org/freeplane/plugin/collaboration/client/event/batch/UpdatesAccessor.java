package org.freeplane.plugin.collaboration.client.event.batch;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

public class UpdatesAccessor {
	public UpdatesAccessor() {
		super();
	}

	public Updates of(MapModel map) {
		return map.getExtension(Updates.class);
	}

	public Updates of(NodeModel node) {
		return of(node.getMap());
	}
}
