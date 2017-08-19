package org.freeplane.plugin.collaboration.client;

import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpdatesSerializer implements UpdatesProcessor {
	private final Consumer<String> consumer;
	private final ObjectMapper objectMapper;
	public UpdatesSerializer(Consumer<String> consumer, ObjectMapper objectMapper) {
		super();
		this.consumer = consumer;
		this.objectMapper = objectMapper;
	}
	@Override
	public void onUpdates(UpdatesFinished event) {
		try {
			final String json = objectMapper.writeValueAsString(event);
			consumer.accept(json);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}


}
