package org.freeplane.plugin.collaboration.client.server;

import java.util.function.Consumer;

import org.freeplane.collaboration.event.batch.Credentials;
import org.freeplane.collaboration.event.batch.MapId;
import org.freeplane.collaboration.event.batch.UpdateBlockCompleted;
import org.immutables.value.Value.Immutable;

@Immutable
public interface Subscription {
	Credentials credentials();
	MapId mapId();
	Consumer<UpdateBlockCompleted> consumer();
}
