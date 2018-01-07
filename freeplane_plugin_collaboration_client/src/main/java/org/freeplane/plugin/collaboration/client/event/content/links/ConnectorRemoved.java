package org.freeplane.plugin.collaboration.client.event.content.links;

import org.freeplane.plugin.collaboration.client.event.NodeUpdated;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableConnectorRemoved.class)
@JsonDeserialize(as = ImmutableConnectorRemoved.class)
public interface ConnectorRemoved extends NodeUpdated{
	String targetId();
}
