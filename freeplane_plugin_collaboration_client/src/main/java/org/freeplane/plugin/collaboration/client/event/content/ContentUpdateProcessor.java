package org.freeplane.plugin.collaboration.client.event.content;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.mindmapmode.NodeContentManipulator;
import org.freeplane.plugin.collaboration.client.event.UpdateProcessor;

public class ContentUpdateProcessor implements UpdateProcessor<ContentUpdated> {

	private final NodeContentManipulator updater;

	public ContentUpdateProcessor(NodeContentManipulator updater) {
		super();
		this.updater = updater;
	}

	@Override
	public void onUpdate(MapModel map, ContentUpdated event) {
		updater.updateContent(map.getNodeForID(event.nodeId()), event.content(), ContentUpdated.EXCLUSIONS);
	}

	@Override
	public Class<ContentUpdated> eventClass() {
		return ContentUpdated.class;
	}
}
