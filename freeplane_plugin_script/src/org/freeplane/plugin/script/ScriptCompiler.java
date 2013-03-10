package org.freeplane.plugin.script;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import org.codehaus.groovy.tools.FileSystemCompiler;
import org.freeplane.core.util.LogUtils;

public class ScriptCompiler {
    public static void compileScriptsOnPath(ArrayList<String> pathElements) {
        final ArrayList<File> groovyFiles = collectGroovyFiles(pathElements);
        if (!groovyFiles.isEmpty())
            compile(groovyFiles);
    }

    private static ArrayList<File> collectGroovyFiles(ArrayList<String> pathElements) {
        final FileFilter groovyFileFilter = createGroovyFileFilter();
        final ArrayList<File> groovyFiles = new ArrayList<File>();
        for (String path : pathElements) {
            appendFilesForPath(groovyFiles, groovyFileFilter, path);
        }
        return groovyFiles;
    }

    private static void appendFilesForPath(ArrayList<File> groovyFiles, FileFilter groovyFileFilter, String path) {
        final File[] files = new File(path).listFiles(groovyFileFilter);
        final boolean isDirectory = (files != null);
        if (isDirectory) {
            for (int i = 0; i < files.length; i++) {
                groovyFiles.add(files[i]);
            }
        }
    }

    private static FileFilter createGroovyFileFilter() {
        final FileFilter groovyFileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".groovy");
            }
        };
        return groovyFileFilter;
    }

    private static void compile(ArrayList<File> groovyFiles) {
        try {
            final FileSystemCompiler fileSystemCompiler = new FileSystemCompiler(
                ScriptingEngine.createCompilerConfiguration());
            fileSystemCompiler.compile(toArray(groovyFiles));
            LogUtils.info("compiled " + groovyFiles);
        }
        catch (Exception e) {
            LogUtils.severe("error compiling " + groovyFiles, e);
        }
    }

    private static File[] toArray(ArrayList<File> groovyFiles) {
        return groovyFiles.toArray(new File[groovyFiles.size()]);
    }
}
