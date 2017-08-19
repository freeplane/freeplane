package org.freeplane.plugin.collaboration.client;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableUpdatesFinished.class)
@JsonDeserialize(as = ImmutableUpdatesFinished.class)
interface  UpdatesFinished {
	static ImmutableUpdatesFinished.Builder builder() {
		return ImmutableUpdatesFinished.builder();
	}
	String mapId();
	Long mapRevision();
	List<MapUpdated> updateEvents();
}
