package org.freeplane.plugin.collaboration.client.server;

import org.immutables.value.Value.Immutable;

@Immutable
abstract public class Session {
	abstract Server server();
	abstract Credentials credentials();
	abstract Synchronizer synchronizer();
	abstract String mapId();
	void leave() {
		
	}
}
