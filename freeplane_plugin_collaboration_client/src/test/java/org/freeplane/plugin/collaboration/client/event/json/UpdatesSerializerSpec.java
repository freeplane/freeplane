package org.freeplane.plugin.collaboration.client.event.json;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;

import org.freeplane.plugin.collaboration.client.event.batch.UpdatesFinished;
import org.freeplane.plugin.collaboration.client.event.json.UpdatesSerializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@RunWith(MockitoJUnitRunner.class)
public class UpdatesSerializerSpec {
	@Mock
	private Consumer<String> consumer;
	
	@Mock
	private ObjectMapper objectMapper;
	
	@Mock
	private ObjectWriter writer;

	@InjectMocks
	private UpdatesSerializer uut;

	@Mock
	private UpdatesFinished event;
	
	@Test
	public void usesObjectMapper() throws Exception {
		when(objectMapper.writer()).thenReturn(writer);
		when(writer.writeValueAsString(event)).thenReturn("json");
		uut.onUpdates(event);
		verify(consumer).accept("json");
	}
}
