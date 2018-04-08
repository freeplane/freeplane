package org.freeplane.plugin.collaboration.client.websocketserver;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.freeplane.collaboration.event.batch.ImmutableMapId;
import org.freeplane.collaboration.event.batch.MapCreateRequest;
import org.freeplane.collaboration.event.batch.MapId;
import org.freeplane.collaboration.event.batch.MapUpdateRequest;
import org.freeplane.collaboration.event.batch.UpdateBlockCompleted;
import org.freeplane.collaboration.event.json.Jackson;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.collaboration.client.server.Server;
import org.freeplane.plugin.collaboration.client.server.Subscription;
import org.freeplane.plugin.collaboration.client.websocketserver.WebSocketServer.BasicClientEndpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebSocketServer implements Server{
	
	BasicClientEndpoint basicClientEndpoint;
	private final ObjectMapper objectMapper;
	final static String SERVER = "ws://localhost:8080/freeplane";
	
	public WebSocketServer()
	{
		objectMapper = Jackson.objectMapper;
	}

	/*
	 * - client connects
	 * - client subscribes [and authenticates]
	 * - client sends update, receives status, 
	 * - [client merges changes from others]
	 */

	private Consumer<UpdateBlockCompleted> consumer;

	@Override
	public MapId createNewMap(MapCreateRequest request) {
		return ImmutableMapId.of("MapId");
	}

	@Override
	public UpdateStatus update(MapUpdateRequest request) {
		try {
			String json = objectMapper.writeValueAsString(request.update());
			basicClientEndpoint.sendMessage(json);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return UpdateStatus.ACCEPTED;
	}

	// needed for receiving msgs from server!
	@Override
	public void subscribe(Subscription subscription) {
		connectToFreeplaneServer();

		consumer = subscription.consumer();
	}

	private void connectToFreeplaneServer() {
		try {
			if(basicClientEndpoint == null) {
				basicClientEndpoint = new BasicClientEndpoint();
			
				WebSocketContainer container = 
						javax.websocket.ContainerProvider.getWebSocketContainer();
				container.connectToServer(basicClientEndpoint, new URI(SERVER));
			}
		} catch (URISyntaxException | DeploymentException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void unsubscribe(Subscription subscription) {
		consumer = null;
	}
	
	// TODO: use Encoder/Decoder to automatically convert JSON
	@ClientEndpoint
	public class BasicClientEndpoint
	{
		Session userSession = null;
		
	    @OnOpen
	    public void onOpen(Session userSession) {
	    	LogUtils.info("opening websocket");
	        this.userSession = userSession;
	        sendMessage("hello world from BasicClientEndpoint.onOpen()!!");
	    }

	    @OnClose
	    public void onClose(Session userSession, CloseReason reason) {
	    	LogUtils.info("closing websocket");
	        this.userSession = null;
	    }

	    /**
	     * Message is received!
	     * 
	     * @param message
	     */
	    @OnMessage
	    public void onMessage(String message) {
	    }

	    public void sendMessage(String message) {
	        this.userSession.getAsyncRemote().sendText(message);
	    }
	}

}
