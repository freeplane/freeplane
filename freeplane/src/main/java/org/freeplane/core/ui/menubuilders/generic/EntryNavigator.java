package org.freeplane.core.ui.menubuilders.generic;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;


public class EntryNavigator {
	private static final String MENU_ALIASES_PROPERTIES = "/menu_aliases.properties";
	private final Map<String, String> aliases = new LinkedHashMap<String, String>();

	public EntryNavigator() {
	}

	public void initFromProperties() {
		try {
			final String content = FileUtils.slurpResource(MENU_ALIASES_PROPERTIES);
			for (String line : content.split("[\n\r]+"))
				parseLine(line);
		}
		catch (IOException e) {
			LogUtils.severe("cannot load " + MENU_ALIASES_PROPERTIES, e);
		}
	}

	private void parseLine(String line) {
		line = line.trim();
		if (line.length() > 0 && ! line.startsWith("#")) {
			String[] words = line.split("\\s*=\\s*");
			if (words.length != 2)
				throw new RuntimeException("parse error in " + MENU_ALIASES_PROPERTIES + " line '" + line + "'");
			addAlias(words[0], words[1]);
		}
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
