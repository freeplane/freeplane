package org.freeplane.plugin.collaboration.client.server;

import org.freeplane.features.map.mindmapmode.MMapModel;
import org.immutables.value.Value.Immutable;

@Immutable
public abstract class Client {
	abstract Server server();
	abstract Credentials credentials();
	abstract Synchronizer synchronizer();

//	One client
//	Connect to server (web socket session)
//	Create a new map on server and get its uuid
//	Send updates to server and receive them back
//	Subscribes to updates for a given map id
//	Receives all updates from server for a given map id
//	From the beginning
//	From given map revision
//	Optimize: server skips overwritten updates of the same element (later)
	
	Session startSession(MMapModel map) {
		return null;
	}
	
	Session joinSession(String mapId) {
		return null;
	}

}
