package org.freeplane.plugin.collaboration.client.event.batch;

import java.util.WeakHashMap;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

public class UpdateBlockGeneratorFactory {
	WeakHashMap<MapModel, Updates> updates = new WeakHashMap<>();
	private final UpdatesProcessor consumer;
	private final int delay;
	private final String userId;

	public UpdateBlockGeneratorFactory(String userId, UpdatesProcessor consumer, int delay) {
		super();
		this.userId = userId;
		this.consumer = consumer;
		this.delay = delay;
	}

	public Updates of(MapModel map) {
		Updates mapUpdates;
		if (updates.containsKey(map))
			mapUpdates = updates.get(map);
		else {
			final ModifiableUpdateHeaderWrapper headerExtension = map
			    .getExtension(ModifiableUpdateHeaderWrapper.class);
			mapUpdates = new Updates(userId, consumer, delay, headerExtension.header);
			updates.put(map, mapUpdates);
		}
		return mapUpdates;
	}

	public Updates of(NodeModel node) {
		return of(node.getMap());
	}
}
