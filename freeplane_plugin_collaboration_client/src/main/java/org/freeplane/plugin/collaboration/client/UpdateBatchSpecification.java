package org.freeplane.plugin.collaboration.client;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableUpdateBatchSpecification.class)
@JsonDeserialize(as = ImmutableUpdateBatchSpecification.class)
interface  UpdateBatchSpecification {
	String mapId();
	Long mapRevision();
	List<UpdateSpecification> updates();
}
