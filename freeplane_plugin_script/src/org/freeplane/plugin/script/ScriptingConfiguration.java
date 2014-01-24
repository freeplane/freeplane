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
	private final TreeMap<String, String> menuTitleToPathMap = new TreeMap<String, String>();
	private final TreeMap<String, ScriptMetaData> menuTitleToMetaDataMap = new TreeMap<String, ScriptMetaData>();

	ScriptingConfiguration() {
	    ScriptResources.setClasspath(buildClasspath());
		addPluginDefaults();
		initMenuTitleToPathMap();
	}

	private void addPluginDefaults() {
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null)
			throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		Controller.getCurrentController().getResourceController().addDefaults(defaults);
	}

	private void initMenuTitleToPathMap() {
		final Map<File, Script> addOnScriptMap = createAddOnScriptMap();
		addAddOnScripts(addOnScriptMap);
		addNonAddOnScripts(addOnScriptMap);
	}

    private void addAddOnScripts(Map<File, Script> addOnScriptMap) {
        for (File file : addOnScriptMap.keySet()) {
            addScript(file, addOnScriptMap);
        }
    }

    private void addNonAddOnScripts(final Map<File, Script> addOnScriptMap) {
        final FilenameFilter scriptFilenameFilter = createFilenameFilter(createScriptRegExp());
		for (File dir : getScriptDirs()) {
            addNonAddOnScripts(dir, addOnScriptMap, scriptFilenameFilter);
		}
    }

	private Map<File, Script> createAddOnScriptMap() {
		Map<File, Script> result = new LinkedHashMap<File, Script>();
		for (ScriptAddOnProperties scriptAddOnProperties : getInstalledScriptAddOns()) {
            final List<Script> scripts = scriptAddOnProperties.getScripts();
            for (Script script : scripts) {
                script.active = scriptAddOnProperties.isActive();
                result.put(findScriptFile(scriptAddOnProperties, script), script);
            }
		}
		return result;
    }

    private List<ScriptAddOnProperties> getInstalledScriptAddOns() {
        final List<ScriptAddOnProperties> installedAddOns = new ArrayList<ScriptAddOnProperties>();
		for (AddOnProperties addOnProperties : AddOnsController.getController().getInstalledAddOns()) {
	        if (addOnProperties.getAddOnType() == AddOnType.SCRIPT) {
	        	installedAddOns.add((ScriptAddOnProperties) addOnProperties);
	        }
        }
        return installedAddOns;
    }

    private File findScriptFile(AddOnProperties addOnProperties, Script script) {
        final File dir = new File(getPrivateAddOnDirectory(addOnProperties), "scripts");
        final File result = new File(dir, script.name);
        return result.exists() ? result : findScriptFile_pre_1_3_x_final(script);
    }

    private File getPrivateAddOnDirectory(AddOnProperties addOnProperties) {
        return new File(AddOnsController.getController().getAddOnsDir(), addOnProperties.getName());
    }

    // add-on scripts are installed in a add-on-private directory since 1.3.x_beta
    @Deprecated
    private File findScriptFile_pre_1_3_x_final(Script script) {
        return new File(ScriptResources.getUserScriptsDir(), script.name);
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
		dirs.add(ScriptResources.getUserScriptsDir());
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
    private void addNonAddOnScripts(final File dir, final Map<File, Script> addOnScriptMap,
                            FilenameFilter filenameFilter) {
        // add all addOn scripts
        // find further scripts in configured directories
        if (dir.isDirectory()) {
            final File[] files = dir.listFiles(filenameFilter);
            if (files != null) {
                for (final File file : files) {
                    if (addOnScriptMap.get(file) == null)
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
		final String menuTitle = disambiguateMenuTitle(getOrCreateMenuTitle(file, scriptConfig));
		try {
			menuTitleToPathMap.put(menuTitle, file.getAbsolutePath());
			final ScriptMetaData metaData = createMetaData(file, menuTitle, scriptConfig);
			menuTitleToMetaDataMap.put(menuTitle, metaData);
			final File parentFile = file.getParentFile();
			if (parentFile.equals(ScriptResources.getBuiltinScriptsDir())) {
				metaData.setPermissions(ScriptingPermissions.getPermissiveScriptingPermissions());
			}
		}
		catch (final IOException e) {
			LogUtils.warn("problems with script " + file.getAbsolutePath(), e);
			menuTitleToPathMap.remove(menuTitle);
			menuTitleToMetaDataMap.remove(menuTitle);
		}
	}

    private String disambiguateMenuTitle(final String menuTitleOrig) {
        String menuTitle = menuTitleOrig;
		// add suffix if the same script exists in multiple dirs
		for (int i = 2; menuTitleToPathMap.containsKey(menuTitle); ++i) {
			menuTitle = menuTitleOrig + i;
		}
        return menuTitle;
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
	private String getOrCreateMenuTitle(final File file, Script scriptConfig) {
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

	SortedMap<String, String> getMenuTitleToPathMap() {
		return Collections.unmodifiableSortedMap(menuTitleToPathMap);
	}

	SortedMap<String, ScriptMetaData> getMenuTitleToMetaDataMap() {
		return Collections.unmodifiableSortedMap(menuTitleToMetaDataMap);
	}

	private ArrayList<String> buildClasspath() {
	    final ArrayList<String> classpath = new ArrayList<String>();
        addClasspathForConfiguredEntries(classpath);
        addClasspathForAddOns(classpath);
        return classpath;
    }

    private void addClasspathForAddOns(final ArrayList<String> classpath) {
        final List<ScriptAddOnProperties> installedScriptAddOns = getInstalledScriptAddOns();
        for (ScriptAddOnProperties scriptAddOnProperties : installedScriptAddOns) {
            final List<String> lib = scriptAddOnProperties.getLib();
            if (lib != null) {
                for (String libEntry : lib) {
                    final File dir = new File(getPrivateAddOnDirectory(scriptAddOnProperties), "lib");
                    classpath.add(new File(dir, libEntry).getAbsolutePath());
                }
            }
        }
    }

    private void addClasspathForConfiguredEntries(final ArrayList<String> classpath) {
        for (File classpathElement : uniqueClassPathElements(ResourceController.getResourceController())) {
            addClasspathElement(classpath, classpathElement);
        }
    }

    private Set<File> uniqueClassPathElements(final ResourceController resourceController) {
        final String classpathString = resourceController.getProperty(ScriptResources.RESOURCES_SCRIPT_CLASSPATH);
        final TreeSet<File> classpathElements = new TreeSet<File>();
        if (classpathString != null) {
            for (String string : ConfigurationUtils.decodeListValue(classpathString, false)) {
                classpathElements.add(createFile(string));
            }
        }
        classpathElements.add(ScriptResources.getUserLibDir());
        return classpathElements;
    }

    private void addClasspathElement(final ArrayList<String> classpath, File classpathElement) {
        final File file = classpathElement;
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
