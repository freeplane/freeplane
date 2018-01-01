package org.freeplane.plugin.collaboration.client.event.content;

import org.freeplane.features.map.MapModel;
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
		return map.addExtensionIfAbsent(ContentUpdateGenerator.class,
			() -> new ContentUpdateGenerator(timerFactory.createTimer(map),  eventFactory));
	}
}