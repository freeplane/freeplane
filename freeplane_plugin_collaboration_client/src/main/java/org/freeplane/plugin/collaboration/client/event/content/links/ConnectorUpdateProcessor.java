package org.freeplane.plugin.collaboration.client.event.content.links;

import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.UpdateProcessor;

public class ConnectorUpdateProcessor implements UpdateProcessor<ConnectorUpdated> {
	private final MLinkController linkController;

	public ConnectorUpdateProcessor(MLinkController linkController) {
		this.linkController = linkController;
	}

	@Override
	public void onUpdate(MapModel map, ConnectorUpdated event) {
		NodeModel node = map.getNodeForID(event.nodeId());
		ConnectorModel connector = event.getConnectorData().connectorFrom(node);
		linkController.swapProperties(connector);
	}

	@Override
	public Class<ConnectorUpdated> eventClass() {
		return ConnectorUpdated.class;
	}
}
