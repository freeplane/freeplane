package org.freeplane.plugin.collaboration.client.event.children;

import org.freeplane.plugin.collaboration.client.event.batch.MapUpdateTimer;

class ChildrenUpdateGeneratorFactory{
	final private UpdateEventFactory eventFactory;
	
	public ChildrenUpdateGeneratorFactory(UpdateEventFactory eventFactory) {
		super();
		this.eventFactory = eventFactory;
	}

	ChildrenUpdateGenerator create(MapUpdateTimer timer) {
		return new ChildrenUpdateGenerator(timer,  eventFactory);
	}
}