package org.freeplane.plugin.collaboration.client.server;

import org.immutables.value.Value.Immutable;

@Immutable
public interface Credentials {
	String userId();
	static Credentials of(String userId) {
		return ImmutableCredentials.builder().userId(userId).build();
	}
}
