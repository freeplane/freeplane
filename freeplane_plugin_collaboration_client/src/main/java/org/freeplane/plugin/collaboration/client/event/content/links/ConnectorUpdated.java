package org.freeplane.plugin.collaboration.client.event.content.links;

import org.freeplane.plugin.collaboration.client.event.NodeUpdated;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableConnectorUpdated.class)
@JsonDeserialize(as = ImmutableConnectorUpdated.class)
public interface ConnectorUpdated extends NodeUpdated{

}
