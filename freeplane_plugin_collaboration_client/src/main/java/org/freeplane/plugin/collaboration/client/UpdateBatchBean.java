package org.freeplane.plugin.collaboration.client;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableUpdateBatchBean.class)
@JsonDeserialize(as = ImmutableUpdateBatchBean.class)
interface  UpdateBatchBean {
	String mapId();
	Long mapRevision();
	List<UpdateBean> updates();
}
