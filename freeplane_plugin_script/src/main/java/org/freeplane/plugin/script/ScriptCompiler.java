package org.freeplane.plugin.script;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.tools.FileSystemCompiler;
import org.freeplane.core.util.LogUtils;

public class ScriptCompiler {
    private final CompilerConfiguration compilerConfiguration;
	private final GroovyClassLoader compilerClassLoader;
	


	public ScriptCompiler() {
		final ScriptClassLoader scriptClassLoader = ScriptClassLoader.createClassLoader();
		compilerClassLoader = new GroovyClassLoader(scriptClassLoader);
		compilerConfiguration = GroovyScript.createCompilerConfiguration();
	}

	public  void compileScriptsOnPath(List<String> pathElements) {
        for (String pathElement : pathElements) {
            final File dir = new File(pathElement);
            if (dir.isDirectory())
                compileScriptsInDirectory(dir);
        }
    }

    private  void compileScriptsInDirectory(File dir) {
        // FIXME: compile .js and the like too
        final Collection<File> files = FileUtils.listFiles(dir, new String[] { "groovy" }, true);
        if (!files.isEmpty())
            compile(dir, files);
    }

    private  void compile(File dir, Collection<File> files) {
        compile(dir, toArray(files));
    }

    private  void compile(File dir, File[] files) {
        try {
			compilerConfiguration.setTargetDirectory(dir);
			final CompilationUnit unit = new CompilationUnit(compilerConfiguration, null, compilerClassLoader);
            new FileSystemCompiler(compilerConfiguration, unit).compile(files);
            LogUtils.info("compiled in " + dir + ": " + createNameList(files));
        }
        catch (Exception e) {
            LogUtils.severe("error compiling in " + dir + createNameList(files), e);
        }
    }

    private  File[] toArray(Collection<File> groovyFiles) {
        return groovyFiles.toArray(new File[groovyFiles.size()]);
    }

    private  ArrayList<String> createNameList(File[] files) {
        final ArrayList<String> names = new ArrayList<String>(files.length);
        for (File file : files) {
            names.add(file.getName());
        }
        return names;
    }
}
