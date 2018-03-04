package org.freeplane.plugin.collaboration.client.event.batch;

import java.util.List;

import org.freeplane.collaboration.event.MapUpdated;

public interface UpdatesProcessor {
	void onUpdates(List<MapUpdated> updateBlock);
}
