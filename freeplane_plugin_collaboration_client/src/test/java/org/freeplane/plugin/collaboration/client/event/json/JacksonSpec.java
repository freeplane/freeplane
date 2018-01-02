package org.freeplane.plugin.collaboration.client.event.json;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.plugin.collaboration.client.TestData;
import org.freeplane.plugin.collaboration.client.event.GenericNodeUpdated;
import org.freeplane.plugin.collaboration.client.event.MapUpdated;
import org.freeplane.plugin.collaboration.client.event.batch.UpdateBlockCompleted;
import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdated;
import org.freeplane.plugin.collaboration.client.event.children.ImmutableChildrenUpdated;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonSpec {
	static ObjectMapper objectMapper = Jackson.objectMapper;
	
	@Test
	public void serializeAndDeserializeUpdateEvent() throws Exception
	{
		ChildrenUpdated uut = createUpdateEvents();
		String json = objectMapper.writeValueAsString(uut);

		MapUpdated bean = objectMapper.readValue(json, MapUpdated.class);

		assertThat(bean).isEqualTo(uut);
	}


	@Test
	public void serializeAndDeserializeGenericUpdateEvent() throws Exception
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
				.content(asList(TestData.RIGHT_CHILD)).build();
	}

	@Test
	public void serializeAndDeserializeUpdatesCompletedEvent() throws Exception
	{
		UpdateBlockCompleted uut = UpdateBlockCompleted.builder()
				.mapId("mapId")
				.mapRevision(1000L)
				.addUpdateBlock(createUpdateEvents())
				.build();
		String json = objectMapper.writeValueAsString(uut);

		UpdateBlockCompleted bean = objectMapper.readValue(json, UpdateBlockCompleted.class);

		assertThat(bean).isEqualTo(uut);
	}
}
