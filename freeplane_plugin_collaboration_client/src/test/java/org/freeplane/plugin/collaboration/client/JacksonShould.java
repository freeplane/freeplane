package org.freeplane.plugin.collaboration.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.plugin.collaboration.client.UpdateBean.ContentType;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonShould {
	ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void serializeAndDeserializeUpdateBean() throws Exception
	{
		UpdateBean uut = createUpdateBean();
		String json = objectMapper.writeValueAsString(uut);

		UpdateBean bean = objectMapper.readValue(json, UpdateBean.class);

		assertThat(bean).isEqualTo(uut);
	}

	protected ImmutableUpdateBean createUpdateBean() {
		return ImmutableUpdateBean.builder()
				.nodeId("id").contentType(ContentType.TEXT)
				.content("content").build();
	}

	@Test
	public void serializeAndDeserializeUpdateBatchBean() throws Exception
	{
		UpdateBatchBean uut = ImmutableUpdateBatchBean.builder()
				.mapId("mapId")
				.mapRevision(1000L)
				.addUpdates(createUpdateBean())
				.build();
		String json = objectMapper.writeValueAsString(uut);

		UpdateBatchBean bean = objectMapper.readValue(json, UpdateBatchBean.class);

		assertThat(bean).isEqualTo(uut);
	}
}
