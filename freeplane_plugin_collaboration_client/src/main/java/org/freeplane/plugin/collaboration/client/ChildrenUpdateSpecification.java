package org.freeplane.plugin.collaboration.client;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableChildrenUpdateSpecification.class)
@JsonDeserialize(as = ImmutableChildrenUpdateSpecification.class)

public interface ChildrenUpdateSpecification extends UpdateSpecification{
	String content();
}
