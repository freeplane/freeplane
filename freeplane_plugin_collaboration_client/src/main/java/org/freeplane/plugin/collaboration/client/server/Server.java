package org.freeplane.plugin.collaboration.client.server;

import java.util.concurrent.CompletableFuture;

import org.freeplane.collaboration.event.messages.MapCreateRequested;
import org.freeplane.collaboration.event.messages.MapId;
import org.freeplane.collaboration.event.messages.MapUpdateProcessed.UpdateStatus;
import org.freeplane.collaboration.event.messages.MapUpdateRequested;

public interface Server {
	CompletableFuture<MapId> createNewMap(MapCreateRequested request);
	CompletableFuture<UpdateStatus> update(MapUpdateRequested request);
	void subscribe(Subscription subscription);
	void unsubscribe(Subscription subscription);
}
