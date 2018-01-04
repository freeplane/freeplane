package org.freeplane.plugin.collaboration.client.event.content;

import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.content.core.CoreUpdateGeneratorFactory;
import org.freeplane.plugin.collaboration.client.event.content.other.ContentUpdateGenerator;
import org.freeplane.plugin.collaboration.client.event.content.other.ContentUpdateGeneratorFactory;

public class ContentUpdateGenerators{
	final private ContentUpdateGeneratorFactory contentUpdateGeneratorFactory;
	final private CoreUpdateGeneratorFactory coreUpdateGeneratorFactory;

	public ContentUpdateGenerators(ContentUpdateGeneratorFactory generatorFactory,  CoreUpdateGeneratorFactory coreUpdateGeneratorFactory) {
		super();
		this.contentUpdateGeneratorFactory = generatorFactory;
		this.coreUpdateGeneratorFactory = coreUpdateGeneratorFactory;
	}

	public void onNodeContentUpdate(NodeChangeEvent event) {
		NodeModel node = event.getNode();
		if(NodeModel.NODE_TEXT.equals(event.getProperty())) {
			coreUpdateGeneratorFactory.generatorOf(node.getMap()).onCoreUpdate(node);
		}
		else
			contentUpdateGeneratorFactory.contentUpdateGeneratorOf(node.getMap()).onNodeContentUpdate(node);
	}

	public void onMapContentUpdate(MapChangeEvent event) {
		MapModel map = event.getMap();
		final ContentUpdateGenerator generator = contentUpdateGeneratorFactory.contentUpdateGeneratorOf(map);
		generator.onMapContentUpdate(map);
	}

}