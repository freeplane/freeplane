package org.freeplane.features.explorer.mindmapmode;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.MapModel;

class NodeAliases implements IExtension {
	private final Map<NodeAlias, Void> aliases = new WeakHashMap<>();
	
	void add(NodeAlias alias) {
		aliases.put(alias, null);
	}
	

	Set<NodeAlias> aliases() {
		return aliases.keySet();
	}


	static NodeAliases of(final MapModel map) {
		NodeAliases aliases = map.getExtension(NodeAliases.class);
		if(aliases == null)
		{
			aliases = new NodeAliases();
			map.addExtension(aliases);
		}
		
		return aliases;
	}

}
