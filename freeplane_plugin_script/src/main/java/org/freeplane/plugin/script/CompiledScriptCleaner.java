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
    private static long calculateLastDependencyModificationTime() {
        long lastModificationTime = ScriptResources.getClasspath().stream()
            .map(File::new)
            .mapToLong(CompiledScriptCleaner::calculateLastDependencyModificationTime)
            .reduce(0, Long::max);
        return lastModificationTime;
    }
    
    private static long calculateLastDependencyModificationTime(File f) {
        if(f.isDirectory()) {
            try {
                return Files.walk(Paths.get(f.toURI()), FileVisitOption.FOLLOW_LINKS)
                .filter(path -> path.endsWith(".class"))
                .map(Path::toFile)
                .mapToLong(File::lastModified)
                .reduce(0, Long::max);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else if(f.getName().endsWith(".jar"))
                return f.lastModified();
        else
            return 0;
        
    }

    private long lastDependencyModificationTime = calculateLastDependencyModificationTime();
    void removeOutdatedCompiledScripts() {
        File[] cacheDirectories = ScriptResources.getCompiledScriptsDir().listFiles();
        if(cacheDirectories != null)
            Stream.of(cacheDirectories)
                .forEach(this::removeOutdated);
    }
    
    private void removeOutdated(File cache) {
        File propertyFile = new File(cache, "compiled.properties");
        File classes = new File(cache, "classes");
        if (propertyFile.exists() && classes.exists()) {
            Properties properties = new Properties();
            try (InputStream in = new FileInputStream(propertyFile)) {
                properties.load(in);
                long compileTime = Long.parseLong(properties.getProperty("time"));
                String source = properties.getProperty("source");
                File sourceFile = new File(source);
                if(! sourceFile.canRead() 
                        || lastDependencyModificationTime > sourceFile.lastModified()
                        || sourceFile.lastModified() >= compileTime) {
                    FileUtils.deleteDirectory(cache);
                }
            } catch (IOException|NumberFormatException e) {
                LogUtils.warn(e);
            }
        }

    }
 

}
