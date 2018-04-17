package org.freeplane.plugin.collaboration.client.websocketserver;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import org.freeplane.collaboration.event.json.Jackson;
import org.freeplane.collaboration.event.messages.Message;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageTextDecoder implements Decoder.Text<Message> {

	private final ObjectMapper objectMapper;
	
	public MessageTextDecoder()
	{
		objectMapper = Jackson.objectMapper;	
	}

	@Override
	public void init(EndpointConfig config) { }

	@Override
	public void destroy() {	}

	@Override
	public Message decode(String string) throws DecodeException {
		Message msg = null;
		try {
			msg = AccessController.doPrivileged(new PrivilegedExceptionAction<Message>() {

				@Override
				public Message run() throws Exception {
					return objectMapper.readValue(string, Message.class);
				}
			});
		} catch (PrivilegedActionException e) {
			throw new DecodeException(string, "Could not deserialize with jackson!", e);
		}
		return msg;
	}

	@Override
	public boolean willDecode(String s) {
		return true;
	}

}
