package org.freeplane.plugin.collaboration.client.session;

import org.freeplane.collaboration.event.batch.MapId;
import org.freeplane.collaboration.event.batch.ModifiableUpdateHeader;
import org.freeplane.collaboration.event.batch.UserId;
import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.plugin.collaboration.client.event.UpdateProcessorChain;
import org.freeplane.plugin.collaboration.client.event.batch.Updates;
import org.freeplane.plugin.collaboration.client.server.Credentials;
import org.freeplane.plugin.collaboration.client.server.ImmutableSubscription;
import org.freeplane.plugin.collaboration.client.server.Server;
import org.freeplane.plugin.collaboration.client.server.Subscription;

public class Session implements IExtension{

	private static final int DELAY = 100;
	private final MapId mapId;
	private final MMapModel map;
	private final Subscription subscription;
	private final Server server;
	public MapId getMapId() {
		return mapId;
	}

	public Session(Server server, Credentials credentials, UpdateProcessorChain updateProcessor, MapId mapId, MMapModel map) {
		this.server = server;
		this.mapId = mapId;
		this.map = map;
		map.addExtension(this);
		final UserId userId = credentials.userId();
		Updates updates = new Updates(userId, ev -> {
			server.update(credentials, ev);
		}, DELAY, ModifiableUpdateHeader.create().setMapId(mapId).setMapRevision(1));
		map.addExtension(this);
		map.addExtension(updates);
		subscription = ImmutableSubscription.builder().credentials(credentials).mapId(mapId).consumer(
			ev -> updateProcessor.onUpdate(map, ev)).build();
		server.subscribe(subscription);
		
	}

	void terminate() {
		map.removeExtension(Session.class);
		map.removeExtension(Updates.class);
		server.unsubscribe(subscription);
	}
}
