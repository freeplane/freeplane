package org.freeplane.plugin.collaboration.client;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonShould {
	ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void serializeAndDeserializeUpdate() throws Exception
	{
		ChildrenUpdated uut = createUpdateEvents();
		String json = objectMapper.writeValueAsString(uut);

		MapUpdated bean = objectMapper.readValue(json, MapUpdated.class);

		assertThat(bean).isEqualTo(uut);
	}


	@Test
	public void serializeAndDeserializeGenericUpdate() throws Exception
	{
		ChildrenUpdated uut = createUpdateEvents();
		String json = objectMapper.writeValueAsString(uut);

		MapUpdated bean = objectMapper.readValue(json, GenericNodeUpdated.class);

		final String genericJson = objectMapper.writeValueAsString(bean);
		assertThat(genericJson).isEqualTo(json);
	}
	
	protected ImmutableChildrenUpdated createUpdateEvents() {
		return ImmutableChildrenUpdated.builder()
				.nodeId("id")
				.content(asList("content")).build();
	}

	@Test
	public void serializeAndDeserializeUpdateBatch() throws Exception
	{
		UpdatesCompleted uut = ImmutableUpdatesCompleted.builder()
				.mapId("mapId")
				.mapRevision(1000L)
				.addUpdateEvents(createUpdateEvents())
				.build();
		String json = objectMapper.writeValueAsString(uut);

		UpdatesCompleted bean = objectMapper.readValue(json, UpdatesCompleted.class);

		assertThat(bean).isEqualTo(uut);
	}
}
