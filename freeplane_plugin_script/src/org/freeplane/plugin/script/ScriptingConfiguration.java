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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngineFactory;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.ConfigurationUtils;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.main.addons.AddOnProperties;
import org.freeplane.main.addons.AddOnProperties.AddOnType;
import org.freeplane.main.addons.AddOnsController;
import org.freeplane.plugin.script.ExecuteScriptAction.ExecutionMode;
import org.freeplane.plugin.script.addons.ScriptAddOnProperties;
import org.freeplane.plugin.script.addons.ScriptAddOnProperties.Script;

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
		private ScriptingPermissions permissions;

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

		protected String getMenuLocation(final ExecutionMode executionMode) {
			return executionModeLocationMap.get(executionMode);
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

		public void setPermissions(ScriptingPermissions permissions) {
			this.permissions = permissions;
        }

		public ScriptingPermissions getPermissions() {
        	return permissions;
        }
	}

	private static final String[] MENU_BAR_SCRIPTS_PARENT_LOCATIONS = {"main_menu_scripting", "node_popup_scripting"};
	private static final String JAR_REGEX = ".+\\.jar$";
	private final TreeMap<String, String> nameScriptMap = new TreeMap<String, String>();
	private final TreeMap<String, ScriptMetaData> nameScriptMetaDataMap = new TreeMap<String, ScriptMetaData>();

	ScriptingConfiguration() {
	    ScriptResources.setClasspath(buildClasspath());
		addPluginDefaults();
		initNameScriptMap();
	}

	private void addPluginDefaults() {
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null)
			throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		Controller.getCurrentController().getResourceController().addDefaults(defaults);
	}

	private void initNameScriptMap() {
		final Map<File, Script> addOnScriptMap = getAddOnScriptMap();
		final FilenameFilter scriptFilenameFilter = createFilenameFilter(createScriptRegExp());
		for (File dir : getScriptDirs()) {
            addScripts(dir, addOnScriptMap, scriptFilenameFilter);
		}
	}

	public Map<File, Script> getAddOnScriptMap() {
		List<AddOnProperties> installedAddOns = AddOnsController.getController().getInstalledAddOns();
		Map<File, Script> result = new LinkedHashMap<File, Script>();
		for (AddOnProperties addOnProperties : installedAddOns) {
	        if (addOnProperties.getAddOnType() == AddOnType.SCRIPT) {
	        	final ScriptAddOnProperties scriptAddOnProperties = (ScriptAddOnProperties) addOnProperties;
	        	final List<Script> scripts = scriptAddOnProperties.getScripts();
	        	for (Script script : scripts) {
	        		script.active = addOnProperties.isActive();
	        		result.put(new File(ScriptResources.getUserScriptDir(), script.name), script);
                }
	        }
        }
		return result;
    }

	private TreeSet<File> getScriptDirs() {
		final ResourceController resourceController = ResourceController.getResourceController();
		final String dirsString = resourceController.getProperty(ScriptResources.RESOURCES_SCRIPT_DIRECTORIES);
		final TreeSet<File> dirs = new TreeSet<File>(); // remove duplicates -> Set
		if (dirsString != null) {
			for (String dir : ConfigurationUtils.decodeListValue(dirsString, false)) {
			    dirs.add(createFile(dir));
            }
		}
		dirs.add(ScriptResources.getBuiltinScriptsDir());
		dirs.add(ScriptResources.getUserScriptDir());
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
    private void addScripts(final File dir, final Map<File, Script> addOnScriptMap,
                            FilenameFilter filenameFilter) {
        if (dir.isDirectory()) {
            final File[] files = dir.listFiles(filenameFilter);
            if (files != null) {
                for (final File file : files) {
                    addScript(file, addOnScriptMap);
                }
            }
        }
        else {
            LogUtils.warn("not a (script) directory: " + dir);
        }
    }

    private String createScriptRegExp() {
        final ArrayList<String> extensions = new ArrayList<String>();
//        extensions.add("clj");
        for (ScriptEngineFactory scriptEngineFactory : GenericScript.getScriptEngineManager().getEngineFactories()) {
            extensions.addAll(scriptEngineFactory.getExtensions());
        }
        LogUtils.info("looking for scripts with the following endings: " + extensions);
        return ".+\\.(" + StringUtils.join(extensions, "|") + ")$";
    }

	private FilenameFilter createFilenameFilter(final String regexp) {
		final FilenameFilter filter = new FilenameFilter() {
			public boolean accept(final File dir, final String name) {
				return name.matches(regexp);
			}
		};
		return filter;
	}

	private void addScript(final File file, final Map<File, Script> addOnScriptMap) {
		final Script scriptConfig = addOnScriptMap.get(file);
		if (scriptConfig != null && !scriptConfig.active) {
			LogUtils.info("skipping deactivated " + scriptConfig);
			return;
		}
		final String scriptName = getScriptName(file, scriptConfig);
		String name = scriptName;
		// add suffix if the same script exists in multiple dirs
		for (int i = 2; nameScriptMap.containsKey(name); ++i) {
			name = scriptName + i;
		}
		try {
			nameScriptMap.put(name, file.getAbsolutePath());
			final ScriptMetaData metaData = createMetaData(file, name, scriptConfig);
			nameScriptMetaDataMap.put(name, metaData);
			final File parentFile = file.getParentFile();
			if (parentFile.equals(ScriptResources.getBuiltinScriptsDir())) {
				metaData.setPermissions(ScriptingPermissions.getPermissiveScriptingPermissions());
//				metaData.setCacheContent(true);
			}
		}
		catch (final IOException e) {
			LogUtils.warn("problems with script " + file.getAbsolutePath(), e);
			nameScriptMap.remove(name);
			nameScriptMetaDataMap.remove(name);
		}
	}

    private ScriptMetaData createMetaData(final File file, final String scriptName,
                                          final Script scriptConfig) throws IOException {
        return scriptConfig == null ? analyseScriptContent(FileUtils.slurpFile(file), scriptName) //
                : createMetaData(scriptName, scriptConfig);
    }

	// not private to enable tests
	ScriptMetaData analyseScriptContent(final String content, final String scriptName) {
		final ScriptMetaData metaData = new ScriptMetaData(scriptName);
		if (ScriptingConfiguration.firstCharIsEquals(content)) {
			// would make no sense
			metaData.removeExecutionMode(ExecutionMode.ON_SINGLE_NODE);
		}
		setExecutionModes(content, metaData);
		setCacheMode(content, metaData);
		return metaData;
	}
	
	private ScriptMetaData createMetaData(final String scriptName, final Script scriptConfig) {
		final ScriptMetaData metaData = new ScriptMetaData(scriptName);
		metaData.removeAllExecutionModes();
		metaData.addExecutionMode(scriptConfig.executionMode, scriptConfig.menuLocation, scriptConfig.menuTitleKey);
//		metaData.setCacheContent(true);
		metaData.setPermissions(scriptConfig.permissions);
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
                metaData.addExecutionMode(ExecutionMode.valueOf(m.group(1).toUpperCase(Locale.ENGLISH)), m.group(2),
                    m.group(3));
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
	private String getScriptName(final File file, Script scriptConfig) {
		if (scriptConfig != null)
			return scriptConfig.menuTitleKey;
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

	private ArrayList<String> buildClasspath() {
        final ResourceController resourceController = ResourceController.getResourceController();
		final ArrayList<String> classpath = new ArrayList<String>();
		final String classpathElements = resourceController.getProperty(ScriptResources.RESOURCES_SCRIPT_CLASSPATH);
		if (classpathElements != null) {
			for (String classpathElement : ConfigurationUtils.decodeListValue(classpathElements, false)) {
				addClasspathElement(classpath, classpathElement);
			}
		}
        return classpath;
    }

    private void addClasspathElement(final ArrayList<String> classpath, String classpathElement) {
        final File file = createFile(classpathElement);
        if (!file.exists()) {
            LogUtils.warn("classpath entry '" + classpathElement + "' doesn't exist. (Use " + File.pathSeparator
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

	List<String> getClasspath() {
		return ScriptResources.getClasspath();
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
