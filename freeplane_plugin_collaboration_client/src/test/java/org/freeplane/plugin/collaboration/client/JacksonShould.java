package org.freeplane.plugin.collaboration.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.plugin.collaboration.client.UpdateSpecification.ContentType;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonShould {
	ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void serializeAndDeserializeUpdate() throws Exception
	{
		UpdateSpecification uut = createUpdate();
		String json = objectMapper.writeValueAsString(uut);

		UpdateSpecification bean = objectMapper.readValue(json, UpdateSpecification.class);

		assertThat(bean).isEqualTo(uut);
	}

	protected ImmutableUpdateSpecification createUpdate() {
		return ImmutableUpdateSpecification.builder()
				.nodeId("id").contentType(ContentType.TEXT)
				.content("content").build();
	}

	@Test
	public void serializeAndDeserializeUpdateBatch() throws Exception
	{
		UpdateBatchSpecification uut = ImmutableUpdateBatchSpecification.builder()
				.mapId("mapId")
				.mapRevision(1000L)
				.addUpdates(createUpdate())
				.build();
		String json = objectMapper.writeValueAsString(uut);

		UpdateBatchSpecification bean = objectMapper.readValue(json, UpdateBatchSpecification.class);

		assertThat(bean).isEqualTo(uut);
	}
}
