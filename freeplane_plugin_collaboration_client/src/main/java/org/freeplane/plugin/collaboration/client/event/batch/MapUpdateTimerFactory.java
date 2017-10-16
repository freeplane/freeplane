package org.freeplane.plugin.collaboration.client.event.batch;

import java.util.WeakHashMap;

import org.freeplane.features.map.MapModel;

public class MapUpdateTimerFactory {
	WeakHashMap<MapModel, MapUpdateTimer> timers = new WeakHashMap<>();
	private UpdatesProcessor consumer;
	private int delay;

	public MapUpdateTimerFactory(UpdatesProcessor consumer, int delay) {
		super();
		this.consumer = consumer;
		this.delay = delay;
	}
	
	public MapUpdateTimer createTimer(MapModel map) {
		MapUpdateTimer timer;
		if(timers.containsKey(map))
			timer = timers.get(map);
		else {
			final ModifiableUpdateHeaderExtension header = map.getExtension(ModifiableUpdateHeaderExtension.class);
			timer = new MapUpdateTimer(consumer, delay, header);
			timers.put(map, timer);
		}
		return timer;

	}

}
