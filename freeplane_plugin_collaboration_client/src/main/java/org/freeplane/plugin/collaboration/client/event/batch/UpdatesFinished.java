package org.freeplane.plugin.collaboration.client.event.batch;

import java.util.List;

import org.freeplane.plugin.collaboration.client.event.ImmutableUpdatesFinished;
import org.freeplane.plugin.collaboration.client.event.MapUpdated;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableUpdatesFinished.class)
@JsonDeserialize(as = ImmutableUpdatesFinished.class)
public interface  UpdatesFinished {
	static ImmutableUpdatesFinished.Builder builder() {
		return ImmutableUpdatesFinished.builder();
	}
	String mapId();
	Long mapRevision();
	List<MapUpdated> updateEvents();
}
