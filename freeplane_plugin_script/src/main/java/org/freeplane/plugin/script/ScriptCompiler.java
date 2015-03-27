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
    public static void compileScriptsOnPath(List<String> pathElements) {
        for (String pathElement : pathElements) {
            final File dir = new File(pathElement);
            if (dir.isDirectory())
                compileScriptsInDirectory(dir);
        }
    }

    private static void compileScriptsInDirectory(File dir) {
        // FIXME: compile .js and the like too
        final Collection<File> files = FileUtils.listFiles(dir, new String[] { "groovy" }, true);
        if (!files.isEmpty())
            compile(dir, files);
    }

    private static void compile(File dir, Collection<File> files) {
        compile(dir, toArray(files));
    }

    private static void compile(File dir, File[] files) {
        try {
			final CompilerConfiguration compilerConfiguration = GroovyScript
			    .createCompilerConfiguration();
            compilerConfiguration.setTargetDirectory(dir);
            final CompilationUnit unit = new CompilationUnit(compilerConfiguration, null, new GroovyClassLoader(
                ScriptingEngine.class.getClassLoader()));
            new FileSystemCompiler(compilerConfiguration, unit).compile(files);
            LogUtils.info("compiled in " + dir + ": " + createNameList(files));
        }
        catch (Exception e) {
            LogUtils.severe("error compiling in " + dir + createNameList(files), e);
        }
    }

    private static File[] toArray(Collection<File> groovyFiles) {
        return groovyFiles.toArray(new File[groovyFiles.size()]);
    }

    private static ArrayList<String> createNameList(File[] files) {
        final ArrayList<String> names = new ArrayList<String>(files.length);
        for (File file : files) {
            names.add(file.getName());
        }
        return names;
    }
}
