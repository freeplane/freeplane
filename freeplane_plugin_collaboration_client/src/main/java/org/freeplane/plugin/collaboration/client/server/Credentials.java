package org.freeplane.plugin.collaboration.client.server;

import org.freeplane.collaboration.event.batch.UserId;
import org.immutables.value.Value.Immutable;

@Immutable
public interface Credentials {
	UserId userId();
	static Credentials of(UserId userId) {
		return ImmutableCredentials.builder().userId(userId).build();
	}
}
