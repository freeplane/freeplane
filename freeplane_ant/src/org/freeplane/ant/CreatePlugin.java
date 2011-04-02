/**
 * CreatePlugin.java
 *
 * Copyright (C) 2010,  Volker Boerchers
 *
 * CreatePlugin.java is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * FormatTranslation.java is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package org.freeplane.ant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/** creates a skeleton of a new Freeplane plugin. */
public class CreatePlugin extends Task {
	private static final String FREEPLANE_PLUGIN_PREFIX = "freeplane_plugin_";
	private String pluginName;
	private Boolean hasAction;
	private File newPluginDir;
	private File pluginTemplateDir;
	private File baseDir;

	public void execute() {
		// Freeplane has no build.xml on root level but only in projects -> use parentDir
		baseDir = (baseDir == null) ? getProject().getBaseDir().getParentFile() : baseDir;
		readAndValidateParameters();
		newPluginDir = new File(baseDir, FREEPLANE_PLUGIN_PREFIX + pluginName);
		if (newPluginDir.exists())
			fatal("won't overwrite output directory " + newPluginDir + " - please remove it first");
		createDirs();
		try {
			createSources();
			createOtherFiles();
		}
		catch (IOException e) {
			throw new BuildException("error creating files: " + e.getMessage(), e);
		}
		finalWords();
	}

	private void readAndValidateParameters() {
		pluginTemplateDir = getPluginTemplateDir();
		if (!pluginTemplateDir.isDirectory())
			fatal("cannot find Freeplane source directory: " + pluginTemplateDir + " does not exist");
		if (pluginName == null) {
			pluginName = TaskUtils.ask(getProject(), "=> Please enter required plugin name:", null);
			assertNotNull(pluginName, "property 'pluginName' is required");
		}
		pluginName = pluginName.replaceAll(FREEPLANE_PLUGIN_PREFIX, "").toLowerCase();
		if (!pluginName.matches("[a-z]+"))
			fatal("plugin name may only contain letters from the range [a-z]");
		if (hasAction == null)
			hasAction = String.valueOf(
			    TaskUtils.multipleChoice(getProject(), "=> Optional: Does this plugin contribute to the GUI?", "yes,no", "yes"))
			    .equalsIgnoreCase("yes");
	}

	private void createDirs() {
		String[] subdirs = { ".settings" //
		        , "ant" //
		        , "lib" //
		        , "META-INF" //
		        , "src" //
		        , "src/org" //
		        , "src/org/freeplane" //
		        , "src/org/freeplane/plugin" //
		        , "src/org/freeplane/plugin/" + pluginName //
		};
		mkdir(newPluginDir);
		for (String dir : subdirs) {
			mkdir(new File(newPluginDir, dir));
		}
	}

	private void createSources() throws IOException {
		if (hasAction)
			createAction();
		createActivator();
	}

	private void createAction() throws IOException {
		final String capPluginName = TaskUtils.firstToUpper(pluginName);
		String source = "" //
		        + "package " + packageName() + ";\n" //
		        + "\n" //
		        + "import java.awt.event.ActionEvent;\n" //
		        + "\n" //
		        + "import org.freeplane.core.controller.Controller;\n" //
		        + "import org.freeplane.core.ui.AFreeplaneAction;\n" //
		        + "import org.freeplane.core.ui.components.UITools;\n" //
		        + "\n" //
		        + "public class " + capPluginName + "Action extends AFreeplaneAction {\n" //
		        + "	private static final long serialVersionUID = 1L;\n" //
		        + "\n" //
		        + "	public " + capPluginName + "Action() {\n" //
		        + "		super(\"" + capPluginName + "\", controller, \"" + capPluginName + "\", null);\n" //
		        + "	}\n" //
		        + "\n" //
		        + "	public void actionPerformed(final ActionEvent e) {\n" //
		        + "		/*TODO: enter your GUI code here*/\n" //
		        + "		UITools.informationMessage(\"Hi!\\n\\tThis is plugin " + capPluginName + "\");\n" //
		        + "	}\n" //
		        + "}\n";
		write(new File(sourceDir(), capPluginName + "Action.java"), source);
	}

	private void createActivator() throws IOException {
		final String registerAction = hasAction ? "				    "
		        + "final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();\n"
		        + "				    menuBuilder.addAnnotatedAction(new " + TaskUtils.firstToUpper(pluginName)
		        + "Action(modeController.getController()));\n" : "";
		String source = "" //
		        + "package " + packageName() + ";\n" //
		        + "\n" //
		        + "import java.util.Hashtable;\n" //
		        + "\n" //
		        + "import org.freeplane.core.ui.MenuBuilder;\n" //
		        + "import org.freeplane.features.common.map.ModeController;\n" //
		        + "import org.freeplane.features.mindmapmode.MModeController;\n" //
		        + "import org.freeplane.main.osgi.IModeControllerExtensionProvider;\n" //
		        + "import org.osgi.framework.BundleActivator;\n" //
		        + "import org.osgi.framework.BundleContext;\n" //
		        + "\n" //
		        + "public class Activator implements BundleActivator {\n" //
		        + "	/*\n" //
		        + "	 * (non-Javadoc)\n" //
		        + "	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)\n" //
		        + "	 */\n" //
		        + "	public void start(final BundleContext context) throws Exception {\n" //
		        + "		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();\n" //
		        + "		props.put(\"mode\", new String[] { MModeController.MODENAME /*TODO: other modes too?*/});\n" //
		        + "		context.registerService(IModeControllerExtensionProvider.class.getName(),\n" //
		        + "		    new IModeControllerExtensionProvider() {\n" //
		        + "			    public void installExtension() {\n" //
		        + registerAction + "			    }\n" //
		        + "		    /*TODO: further initializations*/}, props);\n" //
		        + "	}\n" //
		        + "\n" //
		        + "	/*\n" //
		        + "	 * (non-Javadoc)\n" //
		        + "	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)\n" //
		        + "	 */\n" //
		        + "	public void stop(final BundleContext context) throws Exception {\n" //
		        + "	}\n" //
		        + "}\n";
		write(new File(sourceDir(), "Activator.java"), source);
	}

	private void createOtherFiles() throws IOException {
		String[] files = { //
		".classpath" //
		        , ".project" //
		        , ".settings/org.eclipse.core.resources.prefs" //
		        , ".settings/org.eclipse.core.runtime.prefs" //
		        , ".settings/org.eclipse.jdt.core.prefs" //
		        , ".settings/org.eclipse.pde.core.prefs" //
		        , "ant/ant.properties" //
		        , "ant/build.xml" //
		        , "META-INF/MANIFEST.MF" //
		};
		for (String fileName : files) {
			final String content = TaskUtils.readFile(new File(pluginTemplateDir, fileName));
			final File newFile = new File(newPluginDir, fileName);
			write(newFile, transform(content));
		}
		// build.properties were missing in 1_0_x so don't try to copy them
		write(new File(newPluginDir, "build.properties"), "source.lib/plugin.jar = src/\n");
	}

	private String transform(String content) {
		return content //
		    .replaceAll("<classpathentry kind=\"lib\"[^>]*>\\s*", "") // .classpath special
		    .replaceAll("(jlatexmath.jar = )", "# $1") // ant.properties special
		    .replaceAll("lib/jlatexmath.jar,\\s*(lib/plugin.jar)", "$1") // MANIFEST.MF special
		    .replace("${commons-lang.jar}:${forms.jar}:${SimplyHTML.jar}:${jlatexmath.jar}", "") // build.xml special
		    .replaceAll("latex", pluginName) //
		    .replaceAll("Latex", TaskUtils.firstToUpper(pluginName)) //
		    .replaceAll("LATEX", pluginName.toUpperCase()) //
		;
	}

	private void write(File file, String content) throws IOException {
		Writer output = new BufferedWriter(new FileWriter(file));
		try {
			// assuming that default encoding is OK!
			output.write(content);
		}
		finally {
			output.close();
		}
	}

	private void finalWords() {
		String buildFragment = "  <antcall target=\"makePlugin\" inheritall=\"false\">\n" //
		        + "    <param name=\"anttarget\" value=\"dist\"/>\n" //
		        + "    <param name=\"targetdir\" value=\"plugins\"/>\n" //
		        + "    <param name=\"plugindir\" value=\"freeplane_plugin_" + pluginName + "\"/>\n" //
		        + "    <param name=\"pluginname\" value=\"org.freeplane.plugin." + pluginName + "\"/>\n" //
		        + "  </antcall>\n";
		log("New plugin created in " + newPluginDir);
		log("What next?");
		log("* import plugin into Eclipse via Import... -> Existing Projects into Workspace");
		log("* add required external jars to " + new File(newPluginDir, "lib"));
		log("* add required external jars and required Freeplane projects to classpath");
		log("* search for \"TODO\" in the project and fill the gaps");
		log("* add the following element to freeplane_framework/ant/build.xml:\n" + buildFragment);
	}

	private File sourceDir() {
		return new File(newPluginDir, "src/org/freeplane/plugin/" + pluginName);
	}

	private String packageName() {
		return "org.freeplane.plugin." + pluginName;
	}

	private File getPluginTemplateDir() {
		return new File(baseDir, "freeplane_plugin_latex");
	}

	private void mkdir(File dir) {
		if (!dir.mkdir())
			fatal(("cannot create directory " + dir));
	}

	private void assertNotNull(Object property, String message) {
		if (property == null)
			fatal(message);
	}

	private void fatal(String message) {
		log(message, Project.MSG_ERR);
		throw new BuildException(message);
	}

	// == properties
	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	public File getBaseDir() {
    	return baseDir;
    }

	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}
	
	public void setBaseDir(String baseDir) {
		setBaseDir(new File(baseDir));
	}
	
	public Boolean getHasAction() {
		return hasAction;
	}

	public void setHasAction(Boolean hasAction) {
		this.hasAction = hasAction;
	}
}
