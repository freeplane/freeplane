package org.freeplane.plugin.collaboration.client.event.children;

import org.freeplane.features.map.MapModel;
import org.freeplane.plugin.collaboration.client.event.batch.MapUpdateTimer;
import org.freeplane.plugin.collaboration.client.event.batch.MapUpdateTimerFactory;
import org.freeplane.plugin.collaboration.client.event.content.ContentUpdateEventFactory;

public class ChildrenUpdateGenerators{
	final private StructureUpdateEventFactory eventFactory;
	final private MapUpdateTimerFactory timerFactory;
	final private ContentUpdateEventFactory contentUpdateEventFactory;
	
	public ChildrenUpdateGenerators(MapUpdateTimerFactory timerFactory, StructureUpdateEventFactory eventFactory,
	                                ContentUpdateEventFactory contentUpdateEventFactory) {
		super();
		this.timerFactory = timerFactory;
		this.eventFactory = eventFactory;
		this.contentUpdateEventFactory = contentUpdateEventFactory;
	}

	public ChildrenUpdateGenerator of(MapModel map) {
		ChildrenUpdateGenerator generator = map.getExtension(ChildrenUpdateGenerator.class);
		if(generator == null) {
			final MapUpdateTimer timer = timerFactory.createTimer(map);
			generator = new ChildrenUpdateGenerator(timer,  eventFactory, contentUpdateEventFactory);
			map.addExtension(generator);
		}
		return generator;
	}
}