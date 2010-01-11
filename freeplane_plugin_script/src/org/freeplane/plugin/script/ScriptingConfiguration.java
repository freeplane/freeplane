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
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogTool;
import org.freeplane.plugin.script.ExecuteScriptAction.ExecutionMode;

/**
 * scans for scripts to be registered via {@link ScriptingRegistration}.
 * 
 * @author Volker Boerchers
 */
public class ScriptingConfiguration {
	static class ScriptMetaData {
		private TreeSet<ExecutionMode> executionModes;
		private boolean isCached = false;
		ScriptMetaData() {
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
		public boolean isCached() {
        	return isCached;
        }
		public void setCached(boolean isCached) {
        	this.isCached = isCached;
        }
    }

	private static final String DEFAULT_SCRIPT_DIRECTORIES = "scripts";
	private static final String SCRIPT_REGEX = ".*\\.groovy$";
	// TODO: remove code duplication with LastOpenedList by extracting
	// list property handling into a utility class, e.g. ConfigurationUtils
	private static final String SEPARATOR = File.pathSeparator
			+ File.pathSeparator;
	private TreeMap<String,String> nameScriptMap = new TreeMap<String, String>();
	private TreeMap<String, ScriptMetaData> nameScriptMetaDataMap = new TreeMap<String, ScriptMetaData>();

	ScriptingConfiguration() {
		initNameScriptMap();
	}

	private void initNameScriptMap() {
		final ResourceController resourceController = ResourceController
				.getResourceController();
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
			file = new File(ResourceController.getResourceController()
					.getFreeplaneUserDirectory(), dir);
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
		for (int i = 2; nameScriptMap.containsKey(name); ++i) {
			name = getScriptName(file) + i;
		}
		nameScriptMap.put(name, file.getAbsolutePath());
		addMetaData(file, name);
	}

	private void addMetaData(File file, String name) {
		ScriptMetaData metaData = new ScriptMetaData();
		if (firstCharIsEquals(file)) {
			// would make no sense
			metaData.removeExecutionMode(ExecutionMode.ON_SINGLE_NODE);
		}
		nameScriptMetaDataMap.put(name, metaData);
		// TODO: read optionpanel stuff
    }

	private boolean firstCharIsEquals(File file) {
        try {
        	FileReader in = new FileReader(file);
			final char[] buf = new char[2];
			boolean result = false;
			if ((in.read(buf, 0, 1)) > 0) {
				result = buf[0] == '=';
			}
			in.close(); // that's save enough
			return result;
        }
        catch (IOException e) {
        	return false;
        }
    }

	/** some beautification: remove directory and suffix + make first letter uppercase. */
	private String getScriptName(File file) {
		// TODO: we could add mnemonics handling here! (e.g. by reading '_' as '&')
		String string = file.getName().replaceFirst("\\.[^.]+", "");
		// fixup characters that might cause problems in menus
		string = string.replaceAll("\\s+", "_");
		return string.length() < 2 ? string : string.substring(0, 1).toUpperCase() + string.substring(1);
	}

	SortedMap<String, String> getNameScriptMap() {
		return Collections.unmodifiableSortedMap(nameScriptMap);
	}

	SortedMap<String, ScriptMetaData> getNameScriptMetaDataMap() {
    	return Collections.unmodifiableSortedMap(nameScriptMetaDataMap);
    }
}
