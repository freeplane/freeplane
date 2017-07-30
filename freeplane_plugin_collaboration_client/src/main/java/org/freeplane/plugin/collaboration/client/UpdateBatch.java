package org.freeplane.plugin.collaboration.client;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableUpdateBatch.class)
@JsonDeserialize(as = ImmutableUpdateBatch.class)
interface  UpdateBatch {
	String mapId();
	Long mapRevision();
	List<Update> updates();
}
