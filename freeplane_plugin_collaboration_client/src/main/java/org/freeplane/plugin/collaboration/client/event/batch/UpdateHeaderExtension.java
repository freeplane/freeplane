package org.freeplane.plugin.collaboration.client.event.batch;

import org.freeplane.core.extension.IExtension;
import org.immutables.value.Value;

@Value.Modifiable
public interface UpdateHeaderExtension extends IExtension{
	String mapId();
	long mapRevision();
}
