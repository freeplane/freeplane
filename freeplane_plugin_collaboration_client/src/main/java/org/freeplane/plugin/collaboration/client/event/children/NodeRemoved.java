package org.freeplane.plugin.collaboration.client.event.children;

import org.freeplane.plugin.collaboration.client.event.NodeUpdated;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableNodeRemoved.class)
@JsonDeserialize(as = ImmutableNodeRemoved.class)
public interface NodeRemoved extends NodeUpdated{
	
	static ImmutableNodeRemoved.Builder builder() {
		return ImmutableNodeRemoved.builder();
	}

}
