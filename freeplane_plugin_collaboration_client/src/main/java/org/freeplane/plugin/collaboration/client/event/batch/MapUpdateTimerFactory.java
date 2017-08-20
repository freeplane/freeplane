package org.freeplane.plugin.collaboration.client.event.batch;

import org.freeplane.features.map.MapModel;
import org.freeplane.plugin.collaboration.client.event.children.UpdateEventFactory;

class MapUpdateTimerFactory {
	private UpdatesProcessor consumer;
	private UpdateEventFactory eventFactory;
	private int delay;

	MapUpdateTimerFactory(UpdatesProcessor consumer, UpdateEventFactory eventFactory, int delay) {
		super();
		this.consumer = consumer;
		this.eventFactory = eventFactory;
		this.delay = delay;
	}
	
	MapUpdateTimer create(MapModel map) {
		final ModifiableUpdateHeaderExtension header = map.getExtension(ModifiableUpdateHeaderExtension.class);
		MapUpdateTimer timer = new MapUpdateTimer(consumer, eventFactory, delay, header);
		return timer;
	}

}
