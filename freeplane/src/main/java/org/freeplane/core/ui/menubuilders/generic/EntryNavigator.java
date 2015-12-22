package org.freeplane.core.ui.menubuilders.generic;

import java.util.LinkedHashMap;
import java.util.Map;

public class EntryNavigator {
	private final Map<String, String> aliases = new LinkedHashMap<String, String>();

	public Entry findChildByPath(Entry top, String path) {
		for (Map.Entry<String, String> entry : aliases.entrySet()) {
			final String alias = entry.getKey();
			if (path.startsWith(alias))
				path = entry.getValue() + path.substring(alias.length());
		}
		return top.getChildByPath(path.split("/"));
	}

	public void addAlias(String alias, String path) {
		aliases.put(alias, path);
	}
}
