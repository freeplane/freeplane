package org.freeplane.plugin.collaboration.client.websocketserver;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.freeplane.collaboration.event.batch.ImmutableMapId;
import org.freeplane.collaboration.event.batch.MapCreateRequest;
import org.freeplane.collaboration.event.batch.MapId;
import org.freeplane.collaboration.event.batch.MapUpdateRequest;
import org.freeplane.collaboration.event.batch.UpdateBlockCompleted;
import org.freeplane.plugin.collaboration.client.server.Server;
import org.freeplane.plugin.collaboration.client.server.Subscription;

public class WebSocketServer implements Server{
	
	final static String SERVER = "ws://localhost:8080/freeplane";
//	final WebSocketClientEndpoint clientEndPoint;
	final Session session;
	
	public WebSocketServer()
	{
		try {
//			ClientManager client = ClientManager.createClient();
//			clientEndPoint = new WebSocketClientEndpoint(new URI("ws://localhost:8080/freeplane"));
			WebSocketContainer container = 
					javax.websocket.ContainerProvider.getWebSocketContainer();
			session = container.connectToServer(WebSocketClientEndpoint.class, new URI(SERVER));
			/*
			session.addMessageHandler(new MessageHandler() {
				@Override
		        public void handleMessage(String message) {
		            LogUtils.info(String.format("received Message(%s)", message));
		            final ObjectMapper jacksonObjectMapper = new JacksonConfiguration().objectMapper();
		            try
		            {
		            	UpdateBlockCompleted updateBlock = jacksonObjectMapper.readValue(message, UpdateBlockCompleted.class);
						logger.info(updateBlock.toString());
		            }
		            catch (IOException ex)
		            {
		            	logger.error("Error on deserialization", ex);
		            }
		        }
		    });
		    */
		} catch (URISyntaxException | DeploymentException | IOException e) {
			throw new RuntimeException(e);
		}
	    try {
			session.getBasicRemote().sendText("WebSocket Hello World!");;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Consumer<UpdateBlockCompleted> consumer;

	@Override
	public MapId createNewMap(MapCreateRequest request) {
		return ImmutableMapId.of("MapId");
	}

	@Override
	public UpdateStatus update(MapUpdateRequest request) {
		return UpdateStatus.ACCEPTED;
	}

	@Override
	public void subscribe(Subscription subscription) {
		consumer = subscription.consumer();
	}

	@Override
	public void unsubscribe(Subscription subscription) {
		consumer = null;
	}

}
