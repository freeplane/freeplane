package org.freeplane.core.ui.menubuilders.generic;

import java.util.HashMap;
import java.util.Map;

public class EntryNavigator {
	private final Map<String, String> aliases = new HashMap<String, String>();

	public Entry findChildByPath(Entry top, String path) {
		final String aliasedPath = aliases.get(path);
		final String actualPath;
		if (aliasedPath != null)
			actualPath = aliasedPath;
		else
			actualPath = path;
		return top.getChildByPath(actualPath.split("/"));
	}

	public void addAlias(String alias, String path) {
		aliases.put(alias, path);
	}
}
