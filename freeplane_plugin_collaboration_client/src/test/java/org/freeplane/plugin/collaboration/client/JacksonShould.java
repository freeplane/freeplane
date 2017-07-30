package org.freeplane.plugin.collaboration.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.plugin.collaboration.client.Update.ContentType;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonShould {
	ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void serializeAndDeserializeUpdate() throws Exception
	{
		Update uut = createUpdate();
		String json = objectMapper.writeValueAsString(uut);

		Update bean = objectMapper.readValue(json, Update.class);

		assertThat(bean).isEqualTo(uut);
	}

	protected ImmutableUpdate createUpdate() {
		return ImmutableUpdate.builder()
				.nodeId("id").contentType(ContentType.TEXT)
				.content("content").build();
	}

	@Test
	public void serializeAndDeserializeUpdateBatch() throws Exception
	{
		UpdateBatch uut = ImmutableUpdateBatch.builder()
				.mapId("mapId")
				.mapRevision(1000L)
				.addUpdates(createUpdate())
				.build();
		String json = objectMapper.writeValueAsString(uut);

		UpdateBatch bean = objectMapper.readValue(json, UpdateBatch.class);

		assertThat(bean).isEqualTo(uut);
	}
}
