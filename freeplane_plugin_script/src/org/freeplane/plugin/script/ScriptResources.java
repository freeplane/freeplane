package org.freeplane.plugin.script;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;

public class ScriptResources {
    static final IFreeplaneScriptErrorHandler IGNORING_SCRIPT_ERROR_HANDLER = new IFreeplaneScriptErrorHandler() {
        @Override
        public void gotoLine(final int pLineNumber) {
        }
    };
    private static final String RESOURCES_SCRIPT_COMPILATION_DISABLED_EXTENSIONS = "script_compilation_disabled_extensions";
    private static final String RESOURCE_SCRIPT_CACHE_COMPILED_SCRIPTS = "script_cache_compiled_scripts";
    static final String RESOURCES_SCRIPT_DIRECTORIES = "script_directories";
    static final String RESOURCES_SCRIPT_CLASSPATH = "script_classpath";
    static final String[] SCRIPT_COMPILATION_DISABLED_EXTENSIONS = ResourceController.getResourceController()
        .getProperty(RESOURCES_SCRIPT_COMPILATION_DISABLED_EXTENSIONS, "").split("\\W+");
    private static final String USER_SCRIPTS_DIR = "scripts";
    private static final String BUILTIN_SCRIPTS_DIR = "scripts";
    private static List<String> classpath;
    private static final File builtinScriptsDir = buildBuiltinScriptsDir();
    private static final File userScriptDir = buildUserScriptsDir();

    static public File getUserScriptDir() {
        return userScriptDir;
    }

    static File getBuiltinScriptsDir() {
        return builtinScriptsDir;
    }

    static List<String> getClasspath() {
        return classpath;
    }

    /** allows to set the classpath for scripts. Due to security considerations it's not possible to set
     * this more than once. */
    static void setClasspath(final List<String> newClasspath) {
        if (classpath != null)
            throw new SecurityException("reset of script classpath is forbidden.");
        classpath = Collections.unmodifiableList(newClasspath);
        if (!classpath.isEmpty())
            LogUtils.info("extending script's classpath by " + classpath);
    }

    private static File buildBuiltinScriptsDir() {
        return new File(ResourceController.getResourceController().getInstallationBaseDir(), BUILTIN_SCRIPTS_DIR);
    }

    private static File buildUserScriptsDir() {
        return new File(ResourceController.getResourceController().getFreeplaneUserDirectory(),
            ScriptResources.USER_SCRIPTS_DIR);
    }
}
