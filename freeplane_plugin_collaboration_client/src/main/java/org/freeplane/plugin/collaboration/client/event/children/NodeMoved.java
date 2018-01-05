package org.freeplane.plugin.collaboration.client.event.children;

import org.freeplane.plugin.collaboration.client.event.NodeUpdated;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableNodeMoved.class)
@JsonDeserialize(as = ImmutableNodeMoved.class)
public interface NodeMoved extends NodeUpdated{
	
	static ImmutableNodeMoved.Builder builder() {
		return ImmutableNodeMoved.builder();
	}
	
	NodePosition position();
}
