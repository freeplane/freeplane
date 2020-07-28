package org.freeplane.plugin.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.freeplane.core.util.LogUtils;

class CompiledScriptCleaner {
    private long calculateLastDependencyModificationTime() {
        long lastModificationTime = ScriptResources.getClasspath().stream()
            .map(File::new)
            .mapToLong(this::calculateLastDependencyModificationTime)
            .reduce(0, Long::max);
        return lastModificationTime;
    }
    
    private long calculateLastDependencyModificationTime(File f) {
        final long lastModificationTime;
        if(f.isDirectory()) {
            try {
                lastModificationTime = Files.walk(Paths.get(f.toURI()), FileVisitOption.FOLLOW_LINKS)
                .filter(path -> 
                    path.getFileName().toString().endsWith(".class"))
                .map(Path::toFile)
                .mapToLong(File::lastModified)
                .reduce(0, Long::max);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else if(f.getName().endsWith(".jar"))
            lastModificationTime = f.lastModified();
        else
            lastModificationTime = 0;
        return lastModificationTime;
    }
    
    void removeOutdatedCompiledScripts() {
        File compiledScriptsDir = ScriptResources.getCompiledScriptsDir();
        if(ScriptCompiler.compilesOnlyChangedScriptFiles()) {
            File[] cacheDirectories = compiledScriptsDir.listFiles();
            if(cacheDirectories != null) {
                long lastDependencyModificationTime = calculateLastDependencyModificationTime();
                Stream.of(cacheDirectories)
                    .forEach(cache -> removeOutdated(cache, lastDependencyModificationTime));
            }
        } else {
            try {
                FileUtils.deleteDirectory(compiledScriptsDir);
            } catch (IOException e) {
                LogUtils.warn(e);
            }
        }
    }
    
    private void removeOutdated(File cache, long lastDependencyModificationTime) {
        File propertyFile = new File(cache, "compiled.properties");
        if (propertyFile.exists()) {
            Properties properties = new Properties();
            try (InputStream in = new FileInputStream(propertyFile)) {
                properties.load(in);
                long compileTime = Long.parseLong(properties.getProperty("time"));
                String source = properties.getProperty("source");
                File sourceFile = new File(source);
                if(! sourceFile.canRead() 
                        || lastDependencyModificationTime >= compileTime
                        || sourceFile.lastModified() >= compileTime) {
                    FileUtils.deleteDirectory(cache);
                }
            } catch (IOException|NumberFormatException e) {
                LogUtils.warn(e);
            }
        }

    }
 

}
