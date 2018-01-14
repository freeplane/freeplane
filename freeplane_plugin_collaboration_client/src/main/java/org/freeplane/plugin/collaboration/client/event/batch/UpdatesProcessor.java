package org.freeplane.plugin.collaboration.client.event.batch;

import org.freeplane.collaboration.event.batch.UpdateBlockCompleted;

public interface UpdatesProcessor {
	void onUpdates(UpdateBlockCompleted event);
}
