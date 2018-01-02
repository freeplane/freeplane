package org.freeplane.plugin.collaboration.client.event.children;

import org.freeplane.features.map.MapModel;
import org.freeplane.plugin.collaboration.client.event.batch.UpdateBlockGeneratorFactory;
import org.freeplane.plugin.collaboration.client.event.content.ContentUpdateEventFactory;

public class ChildrenUpdateGenerators{
	final private StructureUpdateEventFactory eventFactory;
	final private UpdateBlockGeneratorFactory timerFactory;
	final private ContentUpdateEventFactory contentUpdateEventFactory;
	
	public ChildrenUpdateGenerators(UpdateBlockGeneratorFactory timerFactory, StructureUpdateEventFactory eventFactory,
	                                ContentUpdateEventFactory contentUpdateEventFactory) {
		super();
		this.timerFactory = timerFactory;
		this.eventFactory = eventFactory;
		this.contentUpdateEventFactory = contentUpdateEventFactory;
	}

	public ChildrenUpdateGenerator of(MapModel map) {
		ChildrenUpdateGenerator generator = map.addExtensionIfAbsent(ChildrenUpdateGenerator.class, 
			() -> new ChildrenUpdateGenerator(timerFactory.of(map),  eventFactory, contentUpdateEventFactory));
		return generator;
	}
}