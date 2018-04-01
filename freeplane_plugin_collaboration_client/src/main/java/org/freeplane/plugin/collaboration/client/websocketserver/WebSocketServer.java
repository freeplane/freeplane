package org.freeplane.plugin.collaboration.client.websocketserver;

import java.util.function.Consumer;

import org.freeplane.collaboration.event.batch.ImmutableMapId;
import org.freeplane.collaboration.event.batch.MapCreateRequest;
import org.freeplane.collaboration.event.batch.MapId;
import org.freeplane.collaboration.event.batch.MapUpdateRequest;
import org.freeplane.collaboration.event.batch.UpdateBlockCompleted;
import org.freeplane.plugin.collaboration.client.server.Server;
import org.freeplane.plugin.collaboration.client.server.Subscription;

public class WebSocketServer implements Server{

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
