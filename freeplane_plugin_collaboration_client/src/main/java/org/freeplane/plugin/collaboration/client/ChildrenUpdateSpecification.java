package org.freeplane.plugin.collaboration.client;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableChildrenUpdateSpecification.class)
@JsonDeserialize(as = ImmutableChildrenUpdateSpecification.class)
public interface ChildrenUpdateSpecification extends UpdateSpecification{
	List<String> content();
}
