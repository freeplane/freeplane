/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Volker Boerchers
 *
 *  This file author is Volker Boerchers
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see &lt;http://www.gnu.org/licenses/&gt;.
 */
package org.freeplane.plugin.script;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.ConfigurationUtils;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.script.ExecuteScriptAction.ExecutionMode;

/**
 * scans for scripts to be registered via {@link ScriptingRegistration}.
 * 
 * @author Volker Boerchers
 */
class ScriptingConfiguration {
	static class ScriptMetaData {
		private final TreeMap<ExecutionMode, String> executionModeLocationMap = new TreeMap<ExecutionMode, String>();
		private final TreeMap<ExecutionMode, String> executionModeTitleKeyMap = new TreeMap<ExecutionMode, String>();
		private boolean cacheContent = false;
		private final String scriptName;

		ScriptMetaData(final String scriptName) {
			this.scriptName = scriptName;
			executionModeLocationMap.put(ExecutionMode.ON_SINGLE_NODE, null);
			executionModeLocationMap.put(ExecutionMode.ON_SELECTED_NODE, null);
			executionModeLocationMap.put(ExecutionMode.ON_SELECTED_NODE_RECURSIVELY, null);
		}

		public Set<ExecutionMode> getExecutionModes() {
			return executionModeLocationMap.keySet();
		}

		public void addExecutionMode(final ExecutionMode executionMode, final String location, final String titleKey) {
			executionModeLocationMap.put(executionMode, location);
			if (titleKey != null)
				executionModeTitleKeyMap.put(executionMode, titleKey);
		}

		public void removeExecutionMode(final ExecutionMode executionMode) {
			executionModeLocationMap.remove(executionMode);
		}

		public void removeAllExecutionModes() {
			executionModeLocationMap.clear();
		}

		public String getMenuLocation(ExecutionMode executionMode) {
			final String specialLocation = executionModeLocationMap.get(executionMode);
			return specialLocation == null ? MENU_BAR_SCRIPTS_LOCATION + "/" + scriptName : specialLocation;
		}

		public String getTitleKey(final ExecutionMode executionMode) {
			final String key = executionModeTitleKeyMap.get(executionMode);
			return key == null ? getExecutionModeKey(executionMode) : key;
		}

		public boolean cacheContent() {
			return cacheContent;
		}

		public void setCacheContent(final boolean cacheContent) {
			this.cacheContent = cacheContent;
		}

		public String getScriptName() {
			return scriptName;
		}
	}

	private static final String[] MENU_BAR_SCRIPTS_PARENT_LOCATIONS = {"main_menu_scripting", "node_popup_scripting"};
	private static final String MENU_BAR_SCRIPTS_LOCATION = MENU_BAR_SCRIPTS_PARENT_LOCATIONS[0] + "/scripts";	
	private static final String SCRIPT_REGEX = ".*\\.groovy$";
	private static final String JAR_REGEX = ".*\\.jar$";
	// or use property script_directories?
	static final String USER_SCRIPTS_DIR = "scripts";
	private final TreeMap<String, String> nameScriptMap = new TreeMap<String, String>();
	private final TreeMap<String, ScriptMetaData> nameScriptMetaDataMap = new TreeMap<String, ScriptMetaData>();
	private ArrayList<String> classpath;

	ScriptingConfiguration() {
		addPluginDefaults();
		initNameScriptMap();
		initClasspath();
	}

	private void addPluginDefaults() {
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null)
			throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		Controller.getCurrentController().getResourceController().addDefaults(defaults);
	}

	private void initNameScriptMap() {
		for (String dir : getScriptDirs()) {
			addScripts(createFile(dir));
		}
	}

	private TreeSet<String> getScriptDirs() {
		final ResourceController resourceController = ResourceController.getResourceController();
		final String dirsString = resourceController.getProperty(ScriptingEngine.RESOURCES_SCRIPT_DIRECTORIES);
		final TreeSet<String> dirs = new TreeSet<String>(); // remove duplicates -> Set
		if (dirsString != null) {
			dirs.addAll(ConfigurationUtils.decodeListValue(dirsString, false));
		}
		final String rootDir = new File(resourceController.getResourceBaseDir()).getAbsoluteFile().getParent();
		dirs.add(new File(rootDir, "scripts").getPath());
		return dirs;
	}

	/**
	 * if <code>path</code> is not an absolute path, prepends the freeplane user
	 * directory to it.
	 */
	private File createFile(final String path) {
		File file = new File(path);
		if (!file.isAbsolute()) {
			file = new File(ResourceController.getResourceController().getFreeplaneUserDirectory(), path);
		}
		return file;
	}

	/** scans <code>dir</code> for script files matching a given rexgex. */
	private void addScripts(final File dir) {
		if (dir.isDirectory()) {
			for (final File file : Arrays.asList(dir.listFiles(createFilenameFilter(SCRIPT_REGEX)))) {
				addScript(file);
			}
		}
		else {
			LogUtils.warn("not a (script) directory: " + dir);
		}
	}

	private FilenameFilter createFilenameFilter(final String regexp) {
		final FilenameFilter filter = new FilenameFilter() {
			public boolean accept(final File dir, final String name) {
				return name.matches(regexp);
			}
		};
		return filter;
	}

	private void addScript(final File file) {
		String name = getScriptName(file);
		try {
			// add suffix if the same script exists in multiple dirs
			for (int i = 2; nameScriptMap.containsKey(name); ++i) {
				name = getScriptName(file) + i;
			}
			nameScriptMap.put(name, file.getAbsolutePath());
			addMetaData(file, name);
		}
		catch (final IOException e) {
			LogUtils.warn("problems with script " + file.getAbsolutePath(), e);
			nameScriptMap.remove(name);
			nameScriptMetaDataMap.remove(name);
		}
	}

	private void addMetaData(final File file, final String name) throws IOException {
		final String content = FileUtils.slurpFile(file);
		final ScriptMetaData metaData = analyseScriptContent(content, name);
		nameScriptMetaDataMap.put(name, metaData);
		// TODO: read optionpanel stuff
	}

	// not private to enable tests
	ScriptMetaData analyseScriptContent(final String content, String scriptName) {
		final ScriptMetaData metaData = new ScriptMetaData(scriptName);
		if (ScriptingConfiguration.firstCharIsEquals(content)) {
			// would make no sense
			metaData.removeExecutionMode(ExecutionMode.ON_SINGLE_NODE);
		}
		setExecutionModes(content, metaData);
		setCacheMode(content, metaData);
		return metaData;
	}

	private void setCacheMode(final String content, final ScriptMetaData metaData) {
		final Pattern cacheScriptPattern = ScriptingConfiguration
		    .makeCaseInsensitivePattern("@CacheScriptContent\\s*\\(\\s*(true|false)\\s*\\)");
		final Matcher matcher = cacheScriptPattern.matcher(content);
		if (matcher.find()) {
			metaData.setCacheContent(new Boolean(matcher.group(1)));
		}
	}

	public static void setExecutionModes(final String content, final ScriptMetaData metaData) {
		final String modeName = StringUtils.join(ExecutionMode.values(), "|");
		final String modeDef = "(?:ExecutionMode\\.)?(" + modeName + ")(?:=\"([^]\"]+)(?:\\[([^]\"]+)\\])?\")?";
		final String modeDefs = "(?:" + modeDef + ",?)+";
		final Pattern pOuter = makeCaseInsensitivePattern("@ExecutionModes\\(\\{(" + modeDefs + ")\\}\\)");
		final Matcher mOuter = pOuter.matcher(content.replaceAll("\\s+", ""));
		if (!mOuter.find()) {
//			System.err.println(metaData.getScriptName() + ": '" + pOuter + "' did not match "
//			        + content.replaceAll("\\s+", ""));
			return;
		}
		metaData.removeAllExecutionModes();
		final Pattern pattern = makeCaseInsensitivePattern(modeDef);
		final String[] locations = mOuter.group(1).split(",");
		for (String match : locations) {
			final Matcher m = pattern.matcher(match);
			if (m.matches()) {
//				System.err.println(metaData.getScriptName() + ":" + m.group(1) + "->" + m.group(2) + "->" + m.group(3));
				metaData.addExecutionMode(ExecutionMode.valueOf(m.group(1).toUpperCase()), m.group(2), m.group(3));
			}
			else {
				LogUtils.severe("script " + metaData.getScriptName() + ": not a menu location: '" + match + "'");
				continue;
			}
		}
	}

	private static boolean firstCharIsEquals(final String content) {
		return content.length() == 0 ? false : content.charAt(0) == '=';
	}

	/** some beautification: remove directory and suffix + make first letter uppercase. */
	private String getScriptName(final File file) {
		// TODO: we could add mnemonics handling here! (e.g. by reading '_' as '&')
		String string = file.getName().replaceFirst("\\.[^.]+", "");
		// fixup characters that might cause problems in menus
		string = string.replaceAll("\\s+", "_");
		return string.length() < 2 ? string : string.substring(0, 1).toUpperCase() + string.substring(1);
	}

	private static Pattern makeCaseInsensitivePattern(final String regexp) {
		return Pattern.compile(regexp, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	}

	SortedMap<String, String> getNameScriptMap() {
		return Collections.unmodifiableSortedMap(nameScriptMap);
	}

	SortedMap<String, ScriptMetaData> getNameScriptMetaDataMap() {
		return Collections.unmodifiableSortedMap(nameScriptMetaDataMap);
	}

	private void initClasspath() {
		final ResourceController resourceController = ResourceController.getResourceController();
		final String entries = resourceController.getProperty(ScriptingEngine.RESOURCES_SCRIPT_CLASSPATH);
		classpath = new ArrayList<String>();
		if (entries != null) {
			for (String entry : ConfigurationUtils.decodeListValue(entries, false)) {
				final File file = createFile(entry);
				if (!file.exists()) {
					LogUtils.warn("classpath entry '" + entry + "' doesn't exist. (Use " + File.pathSeparator
					        + " to separate entries.)");
				}
				else if (file.isDirectory()) {
					classpath.add(file.getAbsolutePath());
					for (final File jar : file.listFiles(createFilenameFilter(JAR_REGEX))) {
						classpath.add(jar.getAbsolutePath());
					}
				}
				else {
					classpath.add(file.getAbsolutePath());
				}
			}
		}
	}

	ArrayList<String> getClasspath() {
		return classpath;
	}

	static String getExecutionModeKey(final ExecuteScriptAction.ExecutionMode executionMode) {
		switch (executionMode) {
			case ON_SINGLE_NODE:
				return "ExecuteScriptOnSingleNode.text";
			case ON_SELECTED_NODE:
				return "ExecuteScriptOnSelectedNode.text";
			case ON_SELECTED_NODE_RECURSIVELY:
				return "ExecuteScriptOnSelectedNodeRecursively.text";
			default:
				throw new AssertionError("unknown ExecutionMode " + executionMode);
		}
	}

	public static String[] getScriptsParentLocations() {
		return MENU_BAR_SCRIPTS_PARENT_LOCATIONS;
	}

	public static String getScriptsLocation(String parentKey) {
		return  parentKey + "/scripts";
	}
}
