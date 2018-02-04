package org.freeplane.plugin.collaboration.client.server;

import java.util.function.Consumer;

import org.freeplane.collaboration.event.batch.UpdateBlockCompleted;
import org.immutables.value.Value.Immutable;

@Immutable
public interface Subscription {
	Credentials credentials();
	String mapId();
	Consumer<UpdateBlockCompleted> consumer();
}
