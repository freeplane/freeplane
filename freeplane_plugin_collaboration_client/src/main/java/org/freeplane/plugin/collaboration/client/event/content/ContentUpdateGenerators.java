package org.freeplane.plugin.collaboration.client.event.content;

import org.freeplane.features.map.MapModel;
import org.freeplane.plugin.collaboration.client.event.batch.MapUpdateTimer;
import org.freeplane.plugin.collaboration.client.event.batch.MapUpdateTimerFactory;

public class ContentUpdateGenerators{
	final private ContentUpdateEventFactory eventFactory;
	final private MapUpdateTimerFactory timerFactory;
	
	public ContentUpdateGenerators(MapUpdateTimerFactory timerFactory, ContentUpdateEventFactory eventFactory) {
		super();
		this.timerFactory = timerFactory;
		this.eventFactory = eventFactory;
	}

	public ContentUpdateGenerator of(MapModel map) {
		ContentUpdateGenerator generator = map.getExtension(ContentUpdateGenerator.class);
		if(generator == null) {
			final MapUpdateTimer timer = timerFactory.createTimer(map);
			generator = new ContentUpdateGenerator(timer,  eventFactory);
			map.addExtension(generator);
		}
		return generator;
	}
}