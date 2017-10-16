package org.freeplane.plugin.collaboration.client.event.children;

import org.freeplane.plugin.collaboration.client.event.batch.MapUpdateTimer;

public class ChildrenUpdateGeneratorFactory{
	final private UpdateEventFactory eventFactory;
	
	public ChildrenUpdateGeneratorFactory(UpdateEventFactory eventFactory) {
		super();
		this.eventFactory = eventFactory;
	}

	public ChildrenUpdateGenerator create(MapUpdateTimer timer) {
		return new ChildrenUpdateGenerator(timer,  eventFactory);
	}
}