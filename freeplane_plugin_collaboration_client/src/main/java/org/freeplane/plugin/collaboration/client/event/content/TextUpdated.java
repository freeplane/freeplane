package org.freeplane.plugin.collaboration.client.event.content;

import org.freeplane.plugin.collaboration.client.event.NodeUpdated;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableTextUpdated.class)
@JsonDeserialize(as = ImmutableTextUpdated.class)
public interface TextUpdated extends NodeUpdated{

	static ImmutableTextUpdated.Builder builder() {
		return ImmutableTextUpdated.builder();
	}

	String content();
	
	ContentType contentType();
}
