package org.freeplane.core.ui.menubuilders.generic;

import java.util.Map;


public class EntryNavigator {
	private final Map<String, String> aliases;
	static EntryNavigator entryNavigator;

	public static EntryNavigator instance() {
		if(entryNavigator != null)
			return entryNavigator;
		entryNavigator = EntryNavigatorFactory.createNavigator();
		return entryNavigator;
	}

	EntryNavigator(Map<String, String> aliases) {
		this.aliases = aliases;
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

	@Override
	public String toString() {
		return String.valueOf(aliases);
	}
}
