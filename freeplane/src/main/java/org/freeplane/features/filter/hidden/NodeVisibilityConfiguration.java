package org.freeplane.features.filter.hidden;

import org.freeplane.core.enumeration.DefaultValueSupplier;
import org.freeplane.core.enumeration.OnceForMap;
import org.freeplane.core.extension.IExtension;

@OnceForMap
public enum NodeVisibilityConfiguration implements IExtension, DefaultValueSupplier<NodeVisibilityConfiguration>{
	SHOW_HIDDEN_NODES
}
