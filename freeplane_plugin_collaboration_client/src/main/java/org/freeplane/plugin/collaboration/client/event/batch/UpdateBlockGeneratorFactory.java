package org.freeplane.plugin.collaboration.client.event.batch;

import java.util.WeakHashMap;

import org.freeplane.features.map.MapModel;

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
		if(generators.containsKey(map))
			generator = generators.get(map);
		else {
			final ModifiableUpdateHeaderExtension header = map.getExtension(ModifiableUpdateHeaderExtension.class);
			generator = new Updates(consumer, delay, header);
			generators.put(map, generator);
		}
		return generator;

	}

}
