package org.freeplane.plugin.collaboration.client.event.children;

import org.freeplane.features.map.MapModel;
import org.freeplane.plugin.collaboration.client.event.batch.MapUpdateTimer;
import org.freeplane.plugin.collaboration.client.event.batch.MapUpdateTimerFactory;

public class ChildrenUpdateGenerators{
	final private UpdateEventFactory eventFactory;
	final private MapUpdateTimerFactory timerFactory;
	
	public ChildrenUpdateGenerators(MapUpdateTimerFactory timerFactory, UpdateEventFactory eventFactory) {
		super();
		this.timerFactory = timerFactory;
		this.eventFactory = eventFactory;
	}

	public ChildrenUpdateGenerator of(MapModel map) {
		ChildrenUpdateGenerator generator = map.getExtension(ChildrenUpdateGenerator.class);
		if(generator == null) {
			final MapUpdateTimer timer = timerFactory.createTimer(map);
			generator = new ChildrenUpdateGenerator(timer,  eventFactory);
			map.addExtension(generator);
		}
		return generator;
	}
}