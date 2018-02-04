package org.freeplane.plugin.collaboration.client.server;

import org.freeplane.collaboration.event.batch.UpdateBlockCompleted;

public interface Server {
	enum UpdateStatus{ACCEPTED, REJECTED, MERGED};
	String createNewMap(Credentials credentials, String name);
	UpdateStatus update(Credentials credentials, UpdateBlockCompleted update);
	void subscribe(Subscription subscription);
	void unsubscribe(Subscription subscription);
}
