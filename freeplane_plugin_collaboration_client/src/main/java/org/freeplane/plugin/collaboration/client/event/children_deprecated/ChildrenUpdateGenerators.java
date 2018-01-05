package org.freeplane.plugin.collaboration.client.event.children_deprecated;

import org.freeplane.features.map.MapModel;
import org.freeplane.plugin.collaboration.client.event.batch.UpdateBlockGeneratorFactory;
import org.freeplane.plugin.collaboration.client.event.content.ContentUpdateGenerators;

public class ChildrenUpdateGenerators{
	final private StructureUpdateEventFactory eventFactory;
	final private UpdateBlockGeneratorFactory updateBlockGeneratorFactory;
	private final ContentUpdateGenerators contentUpdateGenerators;
	
	public ChildrenUpdateGenerators(UpdateBlockGeneratorFactory updateBlockGeneratorFactory, StructureUpdateEventFactory eventFactory,
	                                ContentUpdateGenerators contentUpdateGenerators) {
		super();
		this.updateBlockGeneratorFactory = updateBlockGeneratorFactory;
		this.eventFactory = eventFactory;
		this.contentUpdateGenerators = contentUpdateGenerators;
	}

	public ChildrenUpdateGenerator of(MapModel map) {
		ChildrenUpdateGenerator generator = map.addExtensionIfAbsent(ChildrenUpdateGenerator.class, 
			() -> new ChildrenUpdateGenerator(updateBlockGeneratorFactory.of(map),  eventFactory, contentUpdateGenerators));
		return generator;
	}
}