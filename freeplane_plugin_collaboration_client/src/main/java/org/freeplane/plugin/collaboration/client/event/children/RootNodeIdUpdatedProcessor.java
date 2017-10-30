package org.freeplane.plugin.collaboration.client.event.children;

import org.freeplane.features.map.MapModel;
import org.freeplane.plugin.collaboration.client.event.UpdateProcessor;

public class RootNodeIdUpdatedProcessor implements UpdateProcessor<RootNodeIdUpdated> {

	@Override
	public void onUpdate(MapModel map, RootNodeIdUpdated event) {
		map.getRootNode().setID(event.nodeId());
	}

	@Override
	public Class<RootNodeIdUpdated> eventClass() {
		return RootNodeIdUpdated.class;
	}
}
