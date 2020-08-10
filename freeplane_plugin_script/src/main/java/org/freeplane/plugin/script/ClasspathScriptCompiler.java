package org.freeplane.plugin.script;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.DefaultGroovyStaticMethods;
import org.codehaus.groovy.tools.FileSystemCompiler;
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;

import groovy.lang.GroovyClassLoader;

class ClasspathScriptCompiler {
    private static final String GROOVY = "groovy";
    private static final String JAVA = "java";
	private static final String COMPILED_SCRIPTS_FILE = ".compiledscripts";
	private static final String LAST_JAVA_COMPILER_VERSION = "last_java_compiler_version";
	private static final String COMPILE_ONLY_CHANGED_SCRIPT_FILES = "compile_only_changed_script_files";

	private static boolean isCurrentJavaVersionCompatibleWithCachedClassFiles() {
	    String lastJavaVersion = ResourceController.getResourceController().getProperty(LAST_JAVA_COMPILER_VERSION, null);
	    return lastJavaVersion != null && Compat.JAVA_VERSION.compareTo(lastJavaVersion) >= 0;
	}

	private static CompilerConfiguration createCompilerConfiguration() {
	    final CompilerConfiguration configuration = GroovyScript.createCompilerConfiguration();
	    final File compiledScriptsDir = ScriptResources.getPrecompiledScriptsDir();
	    compiledScriptsDir.mkdirs();
	    configuration.setTargetDirectory(compiledScriptsDir);
	    Map<String, Object> jointOptions = new HashMap<>();
	    configuration.setJointCompilationOptions(jointOptions);
	    return configuration;
	}


	
	private final CompilerConfiguration compilerConfiguration;
	private final GroovyClassLoader compilerClassLoader;
	private PrecompiledClasses oldCompiledFiles;
	private final boolean compileOnlyChangedScriptFiles;



	ClasspathScriptCompiler() {
		final ScriptClassLoader scriptClassLoader = ScriptClassLoader.createClassLoader();
		compilerClassLoader = new GroovyClassLoader(scriptClassLoader);
		compilerConfiguration = createCompilerConfiguration();
		compileOnlyChangedScriptFiles = isCurrentJavaVersionCompatibleWithCachedClassFiles() && compilesOnlyChangedScriptFiles();
	}

	 void compileScriptsOnPath(List<String> pathElements) {
		compileLibraryScripts(pathElements);
        new CompiledScriptCleaner().removeOutdatedCompiledScripts(compileOnlyChangedScriptFiles);
        ResourceController.getResourceController().setProperty(LAST_JAVA_COMPILER_VERSION, Compat.JAVA_VERSION);
    }

    private void compileLibraryScripts(List<String> pathElements) {
        PrecompiledClasses newCompiledFiles = new PrecompiledClasses(System.currentTimeMillis());
        File precompiledScriptsDir = ScriptResources.getPrecompiledScriptsDir();
		final File compiledScriptListFile = compiledScriptListFile(precompiledScriptsDir);
		oldCompiledFiles = PrecompiledClasses.read(compiledScriptListFile);
        for (String pathElement : pathElements) {
            final File dir = new File(pathElement);
            if (dir.isDirectory() && ! precompiledScriptsDir.equals(dir)) {
				final Collection<File> compiledScripts = compileScriptsInDirectory(dir);
				newCompiledFiles.addAll(compiledScripts);
			}
        }
        oldCompiledFiles = null;
		newCompiledFiles.write(compiledScriptListFile);
    }

    static boolean compilesOnlyChangedScriptFiles() {
        return ResourceController.getResourceController().getBooleanProperty(COMPILE_ONLY_CHANGED_SCRIPT_FILES);
    }

	private  Collection<File> compileScriptsInDirectory(File dir) {
		// FIXME: compile .js and the like too
		final Collection<File> allScriptFiles = FileUtils.listFiles(dir, new String[] { GROOVY, JAVA }, true);
		final Collection<File> compiledScriptFiles = compileOnlyChangedScriptFiles ? filterNewFiles(allScriptFiles) : allScriptFiles ;
		if (!compiledScriptFiles.isEmpty()) {
			try {
				GroovyScript.checkAccessPermissionCheckerExists();
				compile(dir, compiledScriptFiles);
				LogUtils.info("compiled in " + dir + ": " + createNameList(allScriptFiles));
			}
			catch (Exception e) {
				LogUtils.severe("error compiling in " + dir + createNameList(compiledScriptFiles), e);
				return Collections.emptyList();
			}
		}
		return allScriptFiles;
	}

	private  void compile(File dir, Collection<File> files) throws Exception {
		File tmpDir = null;
		try {
			tmpDir = DefaultGroovyStaticMethods.createTempDir(null, "groovy-generated-", "-java-source");
			compilerConfiguration.getJointCompilationOptions().put("stubDir", tmpDir);
			compile(dir, toArray(files));
		} finally {
			try {
				if (tmpDir != null) FileSystemCompiler.deleteRecursive(tmpDir);
			} catch (Throwable t) {
				LogUtils.severe("error: could not delete temp files - " + tmpDir.getPath());
			}
		}

	}

	private Collection<File> filterNewFiles(Collection<File> files) {
		return oldCompiledFiles.filterNewAndNewer(files);
	}

	private File compiledScriptListFile(File dir) {
		return new File(dir, COMPILED_SCRIPTS_FILE);
	}

    private  void compile(File dir, File[] files) throws Exception {
    	final CompilationUnit unit = new JavaAwareCompilationUnit(compilerConfiguration, compilerClassLoader, null);
    	new FileSystemCompiler(compilerConfiguration, unit).compile(files);
    }

    private  File[] toArray(Collection<File> groovyFiles) {
        return groovyFiles.toArray(new File[groovyFiles.size()]);
    }

    private  ArrayList<String> createNameList(Collection<File> files) {
        final ArrayList<String> names = new ArrayList<String>(files.size());
        for (File file : files) {
            names.add(file.getName());
        }
        return names;
    }
}
