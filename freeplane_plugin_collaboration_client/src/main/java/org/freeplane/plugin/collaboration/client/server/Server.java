package org.freeplane.plugin.collaboration.client.server;

import org.freeplane.collaboration.event.batch.MapCreateRequest;
import org.freeplane.collaboration.event.batch.MapId;
import org.freeplane.collaboration.event.batch.MapUpdateRequest;

public interface Server {
	enum UpdateStatus{ACCEPTED, REJECTED, MERGED};
	MapId createNewMap(MapCreateRequest request);
	UpdateStatus update(MapUpdateRequest request);
	void subscribe(Subscription subscription);
	void unsubscribe(Subscription subscription);
}
