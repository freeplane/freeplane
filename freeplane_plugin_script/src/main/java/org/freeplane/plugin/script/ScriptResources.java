package org.freeplane.plugin.script;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;

public class ScriptResources {
    static final IFreeplaneScriptErrorHandler IGNORING_SCRIPT_ERROR_HANDLER = new IFreeplaneScriptErrorHandler() {
        @Override
        public void gotoLine(final int pLineNumber) {
        }
    };
    private static final String RESOURCES_SCRIPT_COMPILATION_DISABLED_EXTENSIONS = "script_compilation_disabled_extensions";
    static final String RESOURCES_SCRIPT_DIRECTORIES = "script_directories";
    static final String RESOURCES_SCRIPT_CLASSPATH = "script_classpath";
    static final String[] SCRIPT_COMPILATION_DISABLED_EXTENSIONS = ResourceController.getResourceController()
        .getProperty(RESOURCES_SCRIPT_COMPILATION_DISABLED_EXTENSIONS, "").split("\\W+");

	private static final String USER_SCRIPTS_DIR_PROPERTY = "org.freeplane.scripts.user.dir";
	private static final String USER_SCRIPTS_DIR = System.getProperty(USER_SCRIPTS_DIR_PROPERTY,"scripts");
	private static final String INIT_SCRIPTS_DIR_PROPERTY = "org.freeplane.init.scripts.dir";
	private static final String INIT_SCRIPTS_DIR = System.getProperty(INIT_SCRIPTS_DIR_PROPERTY,"scripts/init");
	private static final String USER_LIB_DIR_PROPERTY = "org.freeplane.scripts.user.lib.dir";
	private static final String USER_LIB_DIR = System.getProperty(USER_LIB_DIR_PROPERTY,"lib");
	private static final String BUILTIN_SCRIPTS_DIR_PROPERTY = "org.freeplane.builtin.scripts.dir";
	private static final String BUILTIN_SCRIPTS_DIR = System.getProperty(BUILTIN_SCRIPTS_DIR_PROPERTY,"scripts");
    private static final String PRECOMPILED_SCRIPTS_DIRECTORY = "compiledscripts";
    private static final String COMPILED_SCRIPTS_DIRECTORY = "compiledscripts2";
    private static List<String> classpath;
    private static final File builtinScriptsDir = buildBuiltinScriptsDir();
    private static final File userScriptsDir = buildUserScriptsDir(ScriptResources.USER_SCRIPTS_DIR);
    private static final File initScriptsDir = buildUserScriptsDir(ScriptResources.INIT_SCRIPTS_DIR);
    private static final File userLibDir = buildUserScriptsDir(ScriptResources.USER_LIB_DIR);


    /** @deprecated use {@link #getUserScriptDir()} instead. */
    public static File getUserScriptDir() {
        return getUserScriptsDir();
    }

    public static File getUserScriptsDir() {
        return userScriptsDir;
    }
    
    public static File getInitScriptsDir() {
        return initScriptsDir;
    }

    public static File getUserLibDir() {
        return userLibDir;
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
		return FileUtils.getAbsoluteFile(ResourceController.getResourceController().getInstallationBaseDir(), BUILTIN_SCRIPTS_DIR);
    }

	private static File buildUserScriptsDir(String userDir) {
        return FileUtils.getAbsoluteFile(ResourceController.getResourceController().getFreeplaneUserDirectory(), userDir);
    }

    static File getPrecompiledScriptsDir() {
        return buildUserScriptsDir(PRECOMPILED_SCRIPTS_DIRECTORY);
    }

    static File getCompiledScriptsDir() {
        return buildUserScriptsDir(COMPILED_SCRIPTS_DIRECTORY);
    }

}
