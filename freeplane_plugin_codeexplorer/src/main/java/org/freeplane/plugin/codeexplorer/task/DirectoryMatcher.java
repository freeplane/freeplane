/*
 * Created on 8 Dec 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;

import static com.tngtech.archunit.core.domain.PackageMatcher.TO_GROUPS;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.freeplane.plugin.codeexplorer.map.CodeNode;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.PackageMatcher;

public class DirectoryMatcher implements SubprojectMatcher{

    public static final DirectoryMatcher ALLOW_ALL = new DirectoryMatcher(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    private final SortedMap<String, String> coreLocationsByPaths;
    private final Collection<File> locations;
    private final Collection<String> subpaths;
    private final List<PackageMatcher> groupMatchers;

    public DirectoryMatcher(Collection<File> locations, Collection<String> subpaths, Collection<String> groupIdentifiers) {
        this.locations = locations;
        this.subpaths = subpaths;
        this.groupMatchers = groupIdentifiers.stream().map(PackageMatcher::of).collect(Collectors.toList());
        coreLocationsByPaths = new TreeMap<>();
        findDirectories((directory, location) -> coreLocationsByPaths.put(directory.toURI().getRawPath(), location.toURI().getRawPath()));
    }

    private void findDirectories(BiConsumer<File, File> consumer) {
        for(File location : locations) {
            if(location.isDirectory()) {
                for (String subPath : subpaths.isEmpty() ? defaultSubpaths(location) : subpaths) {
                    File directory = subPath.equals(".") ? location : new File(location, subPath);
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
    private static String toSubprojectName(String location) {
        Pattern projectName = Pattern.compile("/([^/]+?)!?/(?:(?:bin|build|target)/.*)*$");
        Matcher matcher = projectName.matcher(location);
        if(matcher.find())
            return matcher.group(1);
        else
            return location;
    }

    private Optional<String> identifierByClass(JavaClass javaClass) {
        return groupMatchers.stream().map(
                packageMatcher ->  {
                    final String qualifiedClassName =
                            CodeNode.findEnclosingTopLevelClass(javaClass).getFullName();
                    Optional<PackageMatcher.Result> result = packageMatcher.match(
                            qualifiedClassName);
                    return result.map(TO_GROUPS)
                            .map(List::stream)
                            .map(s -> s.collect(Collectors.joining(":")));})
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
    }


    @Override
    public Optional<SubprojectIdentifier> subprojectIdentifier(JavaClass javaClass) {
        Optional<String> optionalPath = CodeNode.classSourceLocationOf(javaClass);
        final Optional<String> optionalId = optionalPath.map(path -> coreLocationsByPaths.getOrDefault(path, path));
        if(! optionalId.isPresent())
            return Optional.empty();
        if(groupMatchers.isEmpty())
            return optionalId.map(id -> new SubprojectIdentifier(id, toSubprojectName(id)));
        else
            return identifierByClass(javaClass).map(id -> new SubprojectIdentifier(id, id));
    }

    public Collection<File> getImportedLocations() {
        List<File> importedLocations = new ArrayList<>();
        findDirectories((importedLocation, location) -> importedLocations.add(importedLocation));
        return importedLocations;
    }

    public List<String> getFoundLocations(String location) {
        List<String> foundLocations = new ArrayList<>();
        for(Entry<String, String> entry : coreLocationsByPaths.tailMap(location).entrySet()) {
            if(entry.getValue().equals(location))
                foundLocations.add(entry.getKey());
            else
                break;
        }
        return foundLocations;
    }

}
