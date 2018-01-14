package org.freeplane.plugin.collaboration.client.event.content.links;

import org.freeplane.collaboration.event.content.links.ConnectorRemoved;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.UpdateProcessor;

public class ConnectorRemovalProcessor implements UpdateProcessor<ConnectorRemoved> {
	private final MLinkController linkController;
	public ConnectorRemovalProcessor(MLinkController linkController) {
		this.linkController = linkController;
	}

	@Override
	public void onUpdate(MapModel map, ConnectorRemoved event) {
		NodeModel node = map.getNodeForID(event.nodeId());
		linkController.removeArrowLink(linkController.getConnector(node, event.targetId()));
	}

	@Override
	public Class<ConnectorRemoved> eventClass() {
		return ConnectorRemoved.class;
	}
}
