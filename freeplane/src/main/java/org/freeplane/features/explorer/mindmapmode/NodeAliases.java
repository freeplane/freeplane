package org.freeplane.features.explorer.mindmapmode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.MapModel;

class NodeAliases implements IExtension {
	private final Map<NodeAlias, String> aliases = new WeakHashMap<>();
	private final MapModel map;
	
	private NodeAliases(MapModel map) {
		this.map = map;
	}
	
	void add(NodeAlias alias, String id) {
		aliases.put(alias, id);
	}
	

	Collection<NodeAlias> aliases() {
		ArrayList<NodeAlias> list = new ArrayList<>( aliases.size());
		for (Entry<NodeAlias, String> entry:aliases.entrySet()) {
			if(map.getNodeForID(entry.getValue()) != null)
				list.add(entry.getKey());
		}
		return list;
	}


	static NodeAliases of(final MapModel map) {
		NodeAliases aliases = map.getExtension(NodeAliases.class);
		if(aliases == null)
		{
			aliases = new NodeAliases(map);
			map.addExtension(aliases);
		}
		
		return aliases;
	}

}
