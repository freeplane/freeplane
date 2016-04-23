package org.freeplane.core.ui.menubuilders.generic;

import java.util.LinkedHashMap;
import java.util.Map;


public class EntryNavigator {
	private final Map<String, String> aliases = new LinkedHashMap<String, String>();

	public EntryNavigator() {
	}
	
	public Entry findChildByPath(Entry top, String path) {
		final String canonicalPath = replaceAliases(path);
		return top.getChildByPath(canonicalPath.split("/"));
	}

	public String replaceAliases(String path) {
		for (Map.Entry<String, String> entry : aliases.entrySet()) {
			final String alias = entry.getKey();
			if (path.startsWith(alias))
				path = entry.getValue() + path.substring(alias.length());
		}
		return path;
	}

	public void addAlias(String alias, String path) {
		aliases.put(alias, path);
	}

	public String toString() {
		return String.valueOf(aliases);
	}
}
