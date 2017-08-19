package org.freeplane.plugin.collaboration.client;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableUpdatesCompleted.class)
@JsonDeserialize(as = ImmutableUpdatesCompleted.class)
interface  UpdatesCompleted {
	static ImmutableUpdatesCompleted.Builder builder() {
		return ImmutableUpdatesCompleted.builder();
	}
	String mapId();
	Long mapRevision();
	List<MapUpdated> updateEvents();
}
