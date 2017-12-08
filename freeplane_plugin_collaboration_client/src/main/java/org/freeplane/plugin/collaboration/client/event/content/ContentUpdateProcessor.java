package org.freeplane.plugin.collaboration.client.event.content;

import org.freeplane.features.map.MapModel;
import org.freeplane.plugin.collaboration.client.event.UpdateProcessor;

public class ContentUpdateProcessor implements UpdateProcessor<ContentUpdated> {

	public ContentUpdateProcessor() {
	}

	@Override
	public void onUpdate(MapModel map, ContentUpdated event) {
	}

	@Override
	public Class<ContentUpdated> eventClass() {
		return ContentUpdated.class;
	}
}
