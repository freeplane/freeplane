package org.freeplane.plugin.collaboration.client.event.content;

import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.util.TypeReference;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.StyleFactory;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.plugin.collaboration.client.event.UpdateProcessor;

public class CoreUpdateProcessor implements UpdateProcessor<CoreUpdated> {

	private final MTextController textController;

	public CoreUpdateProcessor(MTextController textController) {
		this.textController = textController;
	}

	@Override
	public void onUpdate(MapModel map, CoreUpdated event) {
		NodeModel node = map.getNodeForID(event.nodeId());
		final String content = event.content();
		switch(event.mediaType()) {
		case PLAIN_TEXT:
		case HTML:
			textController.setNodeText(node, content);
			break;
		case LOCALIZED_TEXT:
			textController.setNodeObject(node, StyleFactory.create(TranslatedObject.format(content)));
			break;
		case OBJECT:
			textController.setNodeObject(node, TypeReference.create(content));
			break;
		}
	}

	@Override
	public Class<CoreUpdated> eventClass() {
		return CoreUpdated.class;
	}
}
