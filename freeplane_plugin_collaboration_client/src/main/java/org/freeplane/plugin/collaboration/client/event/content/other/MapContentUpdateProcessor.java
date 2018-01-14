package org.freeplane.plugin.collaboration.client.event.content.other;

import org.freeplane.collaboration.event.content.other.MapContentUpdated;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.mindmapmode.NodeContentManipulator;
import org.freeplane.plugin.collaboration.client.event.UpdateProcessor;

public class MapContentUpdateProcessor implements UpdateProcessor<MapContentUpdated> {

	private final NodeContentManipulator updater;

	public MapContentUpdateProcessor(NodeContentManipulator updater) {
		super();
		this.updater = updater;
	}

	@Override
	public void onUpdate(MapModel map, MapContentUpdated mapContentUpdated) {
		updater.updateMapContent(map, mapContentUpdated.content(), 
			ContentUpdateGenerator.getMapContentExtensions());
	}

	@Override
	public Class<MapContentUpdated> eventClass() {
		return MapContentUpdated.class;
	}
}
