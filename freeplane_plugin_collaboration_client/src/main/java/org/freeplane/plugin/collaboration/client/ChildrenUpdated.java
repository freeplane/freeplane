package org.freeplane.plugin.collaboration.client;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableChildrenUpdated.class)
@JsonDeserialize(as = ImmutableChildrenUpdated.class)
public interface ChildrenUpdated extends NodeUpdated{
	static ImmutableChildrenUpdated.Builder builder() {
		return ImmutableChildrenUpdated.builder();
	}
	
	List<String> content();
}
