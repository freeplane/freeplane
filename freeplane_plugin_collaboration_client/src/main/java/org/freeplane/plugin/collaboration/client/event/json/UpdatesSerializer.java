package org.freeplane.plugin.collaboration.client.event.json;

import java.util.function.Consumer;

import org.freeplane.collaboration.event.batch.UpdateBlockCompleted;
import org.freeplane.collaboration.event.json.Jackson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class UpdatesSerializer {
	private final Consumer<String> consumer;
	private final ObjectMapper objectMapper;
	
	public static UpdatesSerializer of(Consumer<String> consumer){
		return new UpdatesSerializer(consumer, Jackson.objectMapper);
	}
	
	public UpdatesSerializer(Consumer<String> consumer, ObjectMapper objectMapper) {
		super();
		this.consumer = consumer;
		this.objectMapper = objectMapper;
	}
	public void write(UpdateBlockCompleted event) {
		write(event, objectMapper.writer());
	}
	
	public void prettyPrint(UpdateBlockCompleted event) {
		write(event, objectMapper.writerWithDefaultPrettyPrinter());
	}
	private void write(UpdateBlockCompleted event, ObjectWriter writer) {
		try {
			final String json = writer.writeValueAsString(event);
			consumer.accept(json);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}


}
