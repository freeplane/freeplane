/*
 * Created on 8 Dec 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class DirectoryMatcher {
    public static final DirectoryMatcher ALLOW_ALL = new DirectoryMatcher(Collections.emptyList(), Collections.emptyList());
    private final Map<String, String> coreLocationsByPaths;
    private final List<File> locations;
    private final List<String> subpaths;

    public DirectoryMatcher(List<File> locations, List<String> subpaths) {
        this.locations = locations;
        this.subpaths = subpaths;
        coreLocationsByPaths = new HashMap<>(subpaths.isEmpty() ?  locations.size() : locations.size() * subpaths.size());
        findDirectories((directory, location) -> coreLocationsByPaths.put(directory.toURI().getRawPath(), location.toURI().getRawPath()));
    }

    private void findDirectories(BiConsumer<File, File> consumer) {
        for(File location : locations) {
            if(location.isDirectory()) {
                for (String subPath : subpaths.isEmpty() ? defaultSubpaths(location) : subpaths) {
                    File directory = new File(location, subPath);
                    if(directory.isDirectory())
                        consumer.accept(directory, location);
                }
            }
            else
                consumer.accept(location, location);
        }
    }

    private List<String> defaultSubpaths(File location) {
        if (new File(location, "pom.xml").exists())
            return Collections.singletonList("target/classes");
        if (new File(location, "build.gradle").exists())
            return Collections.singletonList("build/classes");
        else
            return Collections.singletonList(".");

    }

    public String coreLocationPath(String path ) {
        return coreLocationsByPaths.getOrDefault(path, path);
    }

    public Optional<String> coreLocationPath(Optional<String> path ) {
        return path.map(this::coreLocationPath);
    }

    public Collection<File> getImportedLocations() {
        List<File> importedLocations = new ArrayList<>();
        findDirectories((importedLocation, location) -> importedLocations.add(importedLocation));
        return importedLocations;
    }

}
