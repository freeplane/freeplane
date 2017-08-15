package org.freeplane.plugin.collaboration.client;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableGenericNodeUpdated.class)
@JsonDeserialize(as = ImmutableGenericNodeUpdated.class)
@JsonTypeInfo( use = JsonTypeInfo.Id.NONE)
public interface GenericNodeUpdated extends NodeUpdated{
	String contentType();
	JsonNode content();
}
