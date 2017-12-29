package org.freeplane.plugin.collaboration.client.event.content;

import org.freeplane.plugin.collaboration.client.event.NodeUpdated;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableNodeContentUpdated.class)
@JsonDeserialize(as = ImmutableNodeContentUpdated.class)
public interface NodeContentUpdated extends NodeUpdated{
	
	static ImmutableNodeContentUpdated.Builder builder() {
		return ImmutableNodeContentUpdated.builder();
	}
	
	String content();
}
