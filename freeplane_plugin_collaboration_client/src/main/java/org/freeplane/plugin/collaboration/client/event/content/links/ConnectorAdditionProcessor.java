package org.freeplane.plugin.collaboration.client.event.content.links;

import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.UpdateProcessor;

public class ConnectorAdditionProcessor implements UpdateProcessor<ConnectorAdded> {

	private final MLinkController linkController;

	public ConnectorAdditionProcessor(MLinkController linkController) {
		this.linkController = linkController;
	}

	@Override
	public void onUpdate(MapModel map, ConnectorAdded event) {
		NodeModel source = map.getNodeForID(event.nodeId());
		NodeModel target = map.getNodeForID(event.targetId());
		linkController.addConnector(source, target);
	}

	@Override
	public Class<ConnectorAdded> eventClass() {
		return ConnectorAdded.class;
	}
}
