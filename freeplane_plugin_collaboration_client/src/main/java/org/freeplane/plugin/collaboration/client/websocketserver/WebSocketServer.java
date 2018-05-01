package org.freeplane.plugin.collaboration.client.websocketserver;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.freeplane.collaboration.event.MapUpdated;
import org.freeplane.collaboration.event.json.Jackson;
import org.freeplane.collaboration.event.messages.MapCreateRequested;
import org.freeplane.collaboration.event.messages.MapCreated;
import org.freeplane.collaboration.event.messages.MapId;
import org.freeplane.collaboration.event.messages.MapUpdateDistributed;
import org.freeplane.collaboration.event.messages.MapUpdateProcessed;
import org.freeplane.collaboration.event.messages.MapUpdateProcessed.UpdateStatus;
import org.freeplane.collaboration.event.messages.MapUpdateRequested;
import org.freeplane.collaboration.event.messages.Message;
import org.freeplane.collaboration.event.messages.UpdateBlockCompleted;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.collaboration.client.server.Server;
import org.freeplane.plugin.collaboration.client.server.Subscription;

public class WebSocketServer implements Server{

	BasicClientEndpoint basicClientEndpoint;
	final static String SERVER = "ws://localhost:8080/freeplane";

	CompletableFuture<MapId> requestedMapIdCompletableFuture;
	CompletableFuture<UpdateStatus> requestedUpdateStatusCompletableFuture;

	private Consumer<UpdateBlockCompleted> consumer;

	public WebSocketServer()
	{
		connectToFreeplaneServer();
	}

	/*
	 * - client connects
	 * - client subscribes [and authenticates]
	 * - client sends update, receives status,
	 * - [client merges changes from others]
	 */

	@Override
	public CompletableFuture<MapId> createNewMap(MapCreateRequested request) {
		LogUtils.warn("WebSocketServer.createNewMap()");
		requestedMapIdCompletableFuture = new CompletableFuture<MapId>();
		basicClientEndpoint.sendMessage(request);
		return requestedMapIdCompletableFuture;
	}

	@Override
	public CompletableFuture<UpdateStatus> update(MapUpdateRequested request) {
		LogUtils.warn("WebSocketServer.update()");
		requestedUpdateStatusCompletableFuture = new CompletableFuture<UpdateStatus>();
		basicClientEndpoint.sendMessage(request);
		return requestedUpdateStatusCompletableFuture;
	}

	// needed for receiving msgs from server!
	@Override
	public void subscribe(Subscription subscription) {
		consumer = subscription.consumer();
		// send message to server with mapid to receive updates for this map
		// TODO: add Message for this!!
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

	@ClientEndpoint(encoders = { MessageTextEncoder.class }, decoders = { MessageTextDecoder.class })
	public class BasicClientEndpoint
	{
		Session userSession = null;

	    @OnOpen
	    public void onOpen(Session userSession) {
	    	LogUtils.info("opening websocket");
	        this.userSession = userSession;
	    }

	    @OnClose
	    public void onClose(Session userSession, CloseReason reason) {
	    	LogUtils.info("closing websocket");
	        this.userSession = null;
	    }
	    
	    @OnMessage
	    public void onMessage(Session session, Message message) {
			if (message instanceof MapCreated)
			{
				MapCreated msgMapCreated = (MapCreated)message;
				LogUtils.warn("MapCreated received -> mapId=" + msgMapCreated.id());
				requestedMapIdCompletableFuture.complete(msgMapCreated.id());
			}
			else if (message instanceof MapUpdateProcessed) // rename MapUpdatedStatusReceived
			{				
				MapUpdateProcessed msgMapUpdateProcessed = (MapUpdateProcessed)message;
				LogUtils.warn("MapUpdateProcessed received -> " + msgMapUpdateProcessed.status());
				requestedUpdateStatusCompletableFuture.complete(msgMapUpdateProcessed.status());
			}
			else if (message instanceof MapUpdateDistributed)
			{
				MapUpdateDistributed msgMapUpdateDistributed = (MapUpdateDistributed)message;
				LogUtils.warn("MapUpdateDistributed received");
				for (MapUpdated mapUpdated : msgMapUpdateDistributed.update().updateBlock())
				{
//					LogUtils.warn("MapUpdateDistributed::MapUpdated: " + mapUpdated.toString());
				}
				// TODO: why is consumer null here?
//				consumer.accept(msgMapUpdateDistributed.update());
			}
	    }

	    public void sendMessage(Message message)
	    {
	    	this.userSession.getAsyncRemote().sendObject(message);
	    }
	}

}
