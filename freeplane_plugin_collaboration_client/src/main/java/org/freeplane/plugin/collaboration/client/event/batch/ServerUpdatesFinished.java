package org.freeplane.plugin.collaboration.client.event.batch;

import java.util.List;

import org.freeplane.plugin.collaboration.client.event.GenericNodeUpdated;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableServerUpdatesFinished.class)
@JsonDeserialize(as = ImmutableServerUpdatesFinished.class)
public interface  ServerUpdatesFinished {
	static ImmutableServerUpdatesFinished.Builder builder() {
		return ImmutableServerUpdatesFinished.builder();
	}
	String mapId();
	long mapRevision();
	List<GenericNodeUpdated> updateEvents();
}
