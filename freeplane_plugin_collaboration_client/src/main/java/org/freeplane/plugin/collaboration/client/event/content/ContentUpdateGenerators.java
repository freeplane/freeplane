package org.freeplane.plugin.collaboration.client.event.content;

import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;

public class ContentUpdateGenerators{
	final private ContentUpdateGeneratorFactory generatorFactory;

	public ContentUpdateGenerators(ContentUpdateGeneratorFactory generatorFactory) {
		super();
		this.generatorFactory = generatorFactory;
	}

	public void onNodeContentUpdate(NodeChangeEvent event) {
		NodeModel node = event.getNode();
		final ContentUpdateGenerator generator = generatorFactory.generatorOf(node.getMap());
		generator.onNodeContentUpdate(node);
	}

	public void onMapContentUpdate(MapChangeEvent event) {
		MapModel map = event.getMap();
		final ContentUpdateGenerator generator = generatorFactory.generatorOf(map);
		generator.onMapContentUpdate(map);
	}

}