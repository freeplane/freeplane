package org.freeplane.core.ui.menubuilders.generic;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;

class EntryNavigatorFactory{
	private static final String MENU_ALIASES_PROPERTIES = "/menu_aliases.properties";
	private static LinkedHashMap<String, String> aliases;
	static EntryNavigator createNavigator() {
		aliases= new LinkedHashMap<String, String>();
		try {
			final String content = FileUtils.slurpResource(MENU_ALIASES_PROPERTIES);
			for (String line : content.split("[\n\r]+"))
				parseLine(line);
		}
		catch (IOException e) {
			LogUtils.severe("cannot load " + MENU_ALIASES_PROPERTIES, e);
		}
		EntryNavigator entryNavigator = new EntryNavigator(aliases);
		return entryNavigator;
	}

	private static void parseLine(String line) {
		line = line.trim();
		if (line.length() > 0 && ! line.startsWith("#")) {
			String[] words = line.split("\\s*=\\s*");
			if (words.length != 2)
				throw new RuntimeException("parse error in " + MENU_ALIASES_PROPERTIES + " line '" + line + "'");
			aliases.put(words[0], words[1]);
		}
	}
	private EntryNavigatorFactory() {};
}