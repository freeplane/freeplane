package org.freeplane.plugin.collaboration.client.event.children;

import org.freeplane.features.map.MapModel;
import org.freeplane.plugin.collaboration.client.event.batch.MapUpdateTimer;

public class ChildrenUpdateGeneratorFactory{
	final private UpdateEventFactory eventFactory;
	
	public ChildrenUpdateGeneratorFactory(UpdateEventFactory eventFactory) {
		super();
		this.eventFactory = eventFactory;
	}

	public ChildrenUpdateGenerator create(MapModel map, MapUpdateTimer timer) {
		ChildrenUpdateGenerator generator = map.getExtension(ChildrenUpdateGenerator.class);
		if(generator == null) {
			generator = new ChildrenUpdateGenerator(timer,  eventFactory);
			map.addExtension(generator);
		}
		return generator;
	}
}