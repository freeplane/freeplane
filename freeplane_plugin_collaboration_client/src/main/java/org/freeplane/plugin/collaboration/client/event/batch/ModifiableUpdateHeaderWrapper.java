package org.freeplane.plugin.collaboration.client.event.batch;

import org.freeplane.collaboration.event.batch.ModifiableUpdateHeader;
import org.freeplane.core.extension.IExtension;

public class ModifiableUpdateHeaderWrapper implements IExtension {
	public final ModifiableUpdateHeader header;

	public ModifiableUpdateHeaderWrapper(ModifiableUpdateHeader header) {
		super();
		this.header = header;
	}
}
