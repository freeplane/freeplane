package org.freeplane.plugin.collaboration.client;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.plugin.collaboration.client.UpdateSpecification.ContentType;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonShould {
	ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void serializeAndDeserializeUpdate() throws Exception
	{
		ChildrenUpdateSpecification uut = createUpdate();
		String json = objectMapper.writeValueAsString(uut);

		UpdateSpecification bean = objectMapper.readValue(json, UpdateSpecification.class);

		assertThat(bean).isEqualTo(uut);
	}


	@Test
	public void serializeAndDeserializeGenericUpdate() throws Exception
	{
		ChildrenUpdateSpecification uut = createUpdate();
		String json = objectMapper.writeValueAsString(uut);

		UpdateSpecification bean = objectMapper.readValue(json, GenericUpdateSpecification.class);

		final String genericJson = objectMapper.writeValueAsString(bean);
		assertThat(genericJson).isEqualTo(json);
	}
	
	protected ImmutableChildrenUpdateSpecification createUpdate() {
		return ImmutableChildrenUpdateSpecification.builder()
				.nodeId("id")
				.content(asList("content")).build();
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
