package org.freeplane.plugin.collaboration.client.event.batch;

import java.util.WeakHashMap;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

public class UpdateBlockGeneratorFactory {
	WeakHashMap<MapModel, Updates> generators = new WeakHashMap<>();
	private UpdatesProcessor consumer;
	private int delay;

	public UpdateBlockGeneratorFactory(UpdatesProcessor consumer, int delay) {
		super();
		this.consumer = consumer;
		this.delay = delay;
	}

	public Updates of(MapModel map) {
		Updates generator;
		if (generators.containsKey(map))
			generator = generators.get(map);
		else {
			final ModifiableUpdateHeaderWrapper headerExtension = map
			    .getExtension(ModifiableUpdateHeaderWrapper.class);
			generator = new Updates(consumer, delay, headerExtension.header);
			generators.put(map, generator);
		}
		return generator;
	}

	public Updates of(NodeModel node) {
		return of(node.getMap());
	}
}
