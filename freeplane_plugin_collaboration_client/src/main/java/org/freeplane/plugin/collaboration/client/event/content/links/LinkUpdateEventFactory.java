package org.freeplane.plugin.collaboration.client.event.content.links;

import java.net.URI;
import java.util.Optional;

import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.MapUpdated;

class LinkUpdateEventFactory {
	public LinkUpdateEventFactory() {
		super();
	}

	MapUpdated createConnectorAddedEvent(NodeModel source, ConnectorModel connector) {
		return ImmutableConnectorAdded.builder().nodeId(source.getID()).targetId(connector.getTargetID()).build();
	}

	MapUpdated createConnectorUpdatedEvent(NodeModel source, ConnectorModel connector) {
		ConnectorData data = ConnectorData.of(connector);
		return ImmutableConnectorUpdated.builder().nodeId(source.getID()).connectorData(data).build();
	}

	MapUpdated createConnectorRemovedEvent(NodeModel source, ConnectorModel connector) {
		return ImmutableConnectorRemoved.builder().nodeId(source.getID()).targetId(connector.getTargetID()).build();
	}

	public MapUpdated createHyperlinkChangedEvent(NodeModel node, Optional<URI> uri) {
		return ImmutableHyperlinkUpdated.builder().nodeId(node.getID()).uri(uri.map(URI::toString)).build();
	}
}
