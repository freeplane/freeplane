package org.freeplane.plugin.collaboration.client.event.content;

import org.freeplane.plugin.collaboration.client.event.NodeUpdated;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableContentUpdated.class)
@JsonDeserialize(as = ImmutableContentUpdated.class)
public interface ContentUpdated extends NodeUpdated{
	

	
	static ImmutableContentUpdated.Builder builder() {
		return ImmutableContentUpdated.builder();
	}
	
	String content();
}
