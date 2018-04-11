package org.freeplane.plugin.collaboration.client.server;

import java.util.function.Consumer;

import org.freeplane.collaboration.event.messages.Credentials;
import org.freeplane.collaboration.event.messages.MapId;
import org.freeplane.collaboration.event.messages.UpdateBlockCompleted;
import org.immutables.value.Value.Immutable;

@Immutable
public interface Subscription {
	Credentials credentials();
	MapId mapId();
	Consumer<UpdateBlockCompleted> consumer();
}
