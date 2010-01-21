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
import java.util.Arrays;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogTool;
import org.freeplane.core.util.ResUtil;
import org.freeplane.plugin.script.ExecuteScriptAction.ExecutionMode;

/**
 * scans for scripts to be registered via {@link ScriptingRegistration}.
 * 
 * @author Volker Boerchers
 */
public class ScriptingConfiguration {
	static class ScriptMetaData {
		private TreeSet<ExecutionMode> executionModes;
		private boolean cacheContent = false;
		private final String scriptName;

		ScriptMetaData(String scriptName) {
			this.scriptName = scriptName;
			executionModes = new TreeSet<ExecutionMode>();
			executionModes.add(ExecutionMode.ON_SINGLE_NODE);
			executionModes.add(ExecutionMode.ON_SELECTED_NODE);
			executionModes.add(ExecutionMode.ON_SELECTED_NODE_RECURSIVELY);
		}

		public TreeSet<ExecutionMode> getExecutionModes() {
			return executionModes;
		}

		public void addExecutionMode(ExecutionMode executionMode) {
			this.executionModes.add(executionMode);
		}

		public void removeExecutionMode(ExecutionMode executionMode) {
			this.executionModes.remove(executionMode);
		}

		public void removeAllExecutionModes() {
			this.executionModes.clear();
		}

		public String getMenuLocation() {
			return ScriptingRegistration.MENU_BAR_SCRIPTING_LOCATION + "/" + scriptName;
		}

		public boolean cacheContent() {
			return cacheContent;
		}

		public void setCacheContent(boolean cacheContent) {
			this.cacheContent = cacheContent;
		}

		public String getScriptName() {
			return scriptName;
		}
	}

	private static final String DEFAULT_SCRIPT_DIRECTORIES = "scripts";
	private static final String SCRIPT_REGEX = ".*\\.groovy$";
	// TODO: remove code duplication with LastOpenedList by extracting
	// list property handling into a utility class, e.g. ConfigurationUtils
	private static final String SEPARATOR = File.pathSeparator + File.pathSeparator;
	private TreeMap<String, String> nameScriptMap = new TreeMap<String, String>();
	private TreeMap<String, ScriptMetaData> nameScriptMetaDataMap = new TreeMap<String, ScriptMetaData>();

	ScriptingConfiguration() {
		initNameScriptMap();
	}

	private void initNameScriptMap() {
		final ResourceController resourceController = ResourceController.getResourceController();
		resourceController.setDefaultProperty(ScriptingEngine.RESOURCES_SCRIPT_DIRECTORIES, DEFAULT_SCRIPT_DIRECTORIES);
		String dirsString = resourceController.getProperty(ScriptingEngine.RESOURCES_SCRIPT_DIRECTORIES);
		if (dirsString != null) {
			String[] dirs = dirsString.split(SEPARATOR);
			for (int i = 0; i < dirs.length; i++) {
				addScripts(getDirectory(dirs[i]));
			}
		}
	}

	/**
	 * if <code>dir</code> is not an absolute dir, prepends the freeplane user
	 * directory to it.
	 */
	private File getDirectory(String dir) {
		File file = new File(dir);
		if (!file.isAbsolute()) {
			file = new File(ResourceController.getResourceController().getFreeplaneUserDirectory(), dir);
		}
		return file;
	}

	/** scans <code>dir</code> for script files matching a given rexgex. */
	private void addScripts(File dir) {
		if (dir.isDirectory()) {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.matches(SCRIPT_REGEX);
				}
			};
			for (File file : Arrays.asList(dir.listFiles(filter))) {
				addScript(file);
			}
		}
		else {
			LogTool.warn("not a (script) directory: " + dir);
		}
	}

	private void addScript(File file) {
		String name = getScriptName(file);
		try {
			// add suffix if the same script exists in multiple dirs
			for (int i = 2; nameScriptMap.containsKey(name); ++i) {
				name = getScriptName(file) + i;
			}
			nameScriptMap.put(name, file.getAbsolutePath());
			addMetaData(file, name);
		}
		catch (IOException e) {
			LogTool.warn("problems with script " + file.getAbsolutePath(), e);
			nameScriptMap.remove(name);
			nameScriptMetaDataMap.remove(name);
		}
	}

	private void addMetaData(File file, String name) throws IOException {
		ScriptMetaData metaData = new ScriptMetaData(name);
		String content = ResUtil.slurpFile(file);
		analyseScriptContent(content, metaData);
		nameScriptMetaDataMap.put(name, metaData);
		// TODO: read optionpanel stuff
	}

	// static + not private to enable tests
	static void analyseScriptContent(String content, ScriptMetaData metaData) {
		if (firstCharIsEquals(content)) {
			// would make no sense
			metaData.removeExecutionMode(ExecutionMode.ON_SINGLE_NODE);
		}
		setExecutionModes(content, metaData);
		setCacheMode(content, metaData);
	}

	private static void setCacheMode(String content, ScriptMetaData metaData) {
		Pattern cacheScriptPattern = makeCaseInsensitivePattern("@CacheScriptContent\\s*\\(\\s*(true|false)\\s*\\)");
		Matcher matcher = cacheScriptPattern.matcher(content);
		if (matcher.find())
			metaData.setCacheContent(new Boolean(matcher.group(1)));
	}

	private static void setExecutionModes(String content, ScriptMetaData metaData) {
		Pattern executionModePattern = makeCaseInsensitivePattern("@ExecutionModes\\s*\\(\\s*\\{([^}]+)\\}\\s*\\)");
		Matcher matcher = executionModePattern.matcher(content);
		if (matcher.find()) {
			metaData.removeAllExecutionModes();
			Pattern onSingleNodePattern = makeCaseInsensitivePattern("\\bON_SINGLE_NODE\\b");
			Pattern onSelectedNodesPattern = makeCaseInsensitivePattern("\\bON_SELECTED_NODE\\b");
			Pattern onSelectedNodesRecursivelyPattern = makeCaseInsensitivePattern("\\bON_SELECTED_NODE_RECURSIVELY\\b");
			String[] split = matcher.group(1).split("\\s*,\\s*");
			for (String mode : split) {
				if (onSingleNodePattern.matcher(mode).find()) {
					metaData.addExecutionMode(ExecutionMode.ON_SINGLE_NODE);
				}
				else if (onSelectedNodesPattern.matcher(mode).find()) {
					metaData.addExecutionMode(ExecutionMode.ON_SELECTED_NODE);
				}
				else if (onSelectedNodesRecursivelyPattern.matcher(mode).find()) {
					metaData.addExecutionMode(ExecutionMode.ON_SELECTED_NODE_RECURSIVELY);
				}
				else {
					LogTool.warn(metaData.getScriptName() + ": ignoring unknown ExecutionMode '" + mode + "'");
				}
			}
		}
	}

	private static boolean firstCharIsEquals(String content) {
		return content.length() == 0 ? false : content.charAt(0) == '=';
	}

	/** some beautification: remove directory and suffix + make first letter uppercase. */
	private String getScriptName(File file) {
		// TODO: we could add mnemonics handling here! (e.g. by reading '_' as '&')
		String string = file.getName().replaceFirst("\\.[^.]+", "");
		// fixup characters that might cause problems in menus
		string = string.replaceAll("\\s+", "_");
		return string.length() < 2 ? string : string.substring(0, 1).toUpperCase() + string.substring(1);
	}

	private static Pattern makeCaseInsensitivePattern(String regexp) {
		return Pattern.compile(regexp, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	}

	SortedMap<String, String> getNameScriptMap() {
		return Collections.unmodifiableSortedMap(nameScriptMap);
	}

	SortedMap<String, ScriptMetaData> getNameScriptMetaDataMap() {
		return Collections.unmodifiableSortedMap(nameScriptMetaDataMap);
	}
}
