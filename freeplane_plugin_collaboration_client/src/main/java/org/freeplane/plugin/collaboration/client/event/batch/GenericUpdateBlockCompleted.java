package org.freeplane.plugin.collaboration.client.event.batch;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Value.Immutable
@JsonSerialize(as = ImmutableServerUpdatesFinished.class)
@JsonDeserialize(as = ImmutableServerUpdatesFinished.class)
public interface  GenericUpdateBlockCompleted {
	static ImmutableGenericUpdateBlockCompleted.Builder builder() {
		return ImmutableGenericUpdateBlockCompleted.builder();
	}
	String mapId();
	long mapRevision();
	List<ObjectNode> updateBlock();
}
