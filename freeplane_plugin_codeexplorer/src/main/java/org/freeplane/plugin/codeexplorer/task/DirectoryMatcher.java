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
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeplane.plugin.codeexplorer.map.CodeNode;

import com.tngtech.archunit.core.domain.JavaClass;

public class DirectoryMatcher implements GroupMatcher{

    public static final DirectoryMatcher ALLOW_ALL = new DirectoryMatcher(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    private final SortedMap<String, String> coreLocationsByPaths;
    private final Collection<File> locations;
    private final Collection<String> subpaths;
    private final Collection<ClassNameMatcher> groupMatchers;
    private final boolean groupsClassesByName;

    public DirectoryMatcher(Collection<File> locations, Collection<String> subpaths, Collection<ClassNameMatcher> groupMatchers) {
        this.locations = locations;
        this.subpaths = subpaths;
        this.groupMatchers = groupMatchers;
        groupsClassesByName = ! groupMatchers.stream().allMatch(ClassNameMatcher::ignoresClasses);
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
    private static String toGroupName(String location) {
        Pattern projectName = Pattern.compile("/([^/]+?)!?/(?:(?:bin|build|target)/.*)*$");
        Matcher matcher = projectName.matcher(location);
        if(matcher.find())
            return matcher.group(1);
        else
            return location;
    }

    private Optional<String> identifierByClass(JavaClass javaClass) {
        for (ClassNameMatcher groupMatcher : groupMatchers) {
            final String qualifiedClassName = qualifiedClassName(javaClass);
            if(groupMatcher.isIgnored(qualifiedClassName))
                return Optional.empty();
            Optional<String> groupResult = groupMatcher.toGroup(qualifiedClassName);
            if (groupResult.isPresent()) {
                return groupResult;
            }
        }
        return Optional.of("");
    }

    private String qualifiedClassName(JavaClass javaClass) {
        final String fullName = CodeNode.findEnclosingTopLevelClass(javaClass).getName();
        int lastIndexOfNon$ = fullName.length() - 1;
        while (lastIndexOfNon$ > 0 && fullName.charAt(lastIndexOfNon$) == '$')
            lastIndexOfNon$--;

        return fullName.substring(0, lastIndexOfNon$ + 1);
    }


    @Override
    public Optional<GroupIdentifier> groupIdentifier(JavaClass javaClass) {
        Optional<String> optionalPath = CodeNode.classSourceLocationOf(javaClass);
        final Optional<String> optionalCoreLocation = optionalPath.map(path -> coreLocationsByPaths.getOrDefault(path, path));
        if(! optionalCoreLocation.isPresent())
            return Optional.empty();
        final String coreLocation = optionalCoreLocation.get();
        if(groupMatchers.isEmpty() || ! groupsClassesByName && identifierByClass(javaClass).isPresent())
            return Optional.of(new GroupIdentifier(coreLocation, toGroupName(coreLocation)));
        else if (groupsClassesByName)
            return identifierByClass(javaClass)
                    .map(id -> id.isEmpty() ? "*" : id)
                    .map(id -> new GroupIdentifier(id, id));
        else
            return Optional.empty();
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
