package org.freeplane.plugin.collaboration.client.websocketserver;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import org.freeplane.collaboration.event.json.Jackson;
import org.freeplane.collaboration.event.messages.Message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageTextEncoder implements Encoder.Text<Message> {

	private final ObjectMapper objectMapper;
	
	public MessageTextEncoder()
	{
		objectMapper = Jackson.objectMapper;	
	}
	
	@Override
	public void init(EndpointConfig config) { }

	@Override
	public void destroy() {	}

	@Override
	public String encode(Message object) throws EncodeException {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new EncodeException(object, "Could not serialize with jackson!", e);
		}
	}

}
