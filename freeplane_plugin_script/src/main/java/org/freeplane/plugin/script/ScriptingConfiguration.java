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
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.ConfigurationUtils;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.MenuUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.main.addons.AddOnProperties;
import org.freeplane.main.addons.AddOnProperties.AddOnType;
import org.freeplane.main.addons.AddOnsController;
import org.freeplane.plugin.script.addons.ScriptAddOnProperties;

/**
 * scans for scripts to be registered via {@link ScriptingRegistration}.
 *
 * @author Volker Boerchers
 */
class ScriptingConfiguration {

	private static final String JAR_REGEX = ".+\\.jar$";
    private static Map<String, Object> staticProperties = createStaticProperties();

	ScriptingConfiguration() {
	    ScriptResources.setClasspath(buildClasspath());
		addPluginDefaults();
	}

	private void addPluginDefaults() {
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null)
			throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		Controller.getCurrentController().getResourceController().addDefaults(defaults);
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

    private File getPrivateAddOnDirectory(AddOnProperties addOnProperties) {
        return new File(AddOnsController.getController().getAddOnsDir(), addOnProperties.getName());
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

	private FilenameFilter createFilenameFilter(final String regexp) {
		final FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				return name.matches(regexp);
			}
		};
		return filter;
	}
	private ArrayList<String> buildClasspath() {
	    final ArrayList<String> classpath = new ArrayList<String>();
	    classpath.add(ScriptResources.getCompiledScriptsDir().getAbsolutePath());
	    addClasspathForAddOns(classpath);
        addClasspathForConfiguredEntries(classpath);
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
        final Set<File> classpathElements = new LinkedHashSet<File>();
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

	public static Map<String, Object> getStaticProperties() {
        return staticProperties;
    }

    private static Map<String, Object> createStaticProperties() {
        Map<String, Object> properties = new LinkedHashMap<String, Object>();
    	properties.put("logger", new LogUtils());
    	properties.put("ui", new UITools());
    	properties.put("htmlUtils", HtmlUtils.getInstance());
    	properties.put("textUtils", new TextUtils());
    	properties.put("menuUtils", new MenuUtils());
    	properties.put("config", new FreeplaneScriptBaseClass.ConfigProperties());
        return properties;
    }
}
