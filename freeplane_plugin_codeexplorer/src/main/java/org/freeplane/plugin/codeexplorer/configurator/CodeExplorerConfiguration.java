/*
 * Created on 25 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.configurator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.Location;

public class CodeExplorerConfiguration {
    private static final String PROJECT_PART_DELIMITER = "\t";
    private String projectName;
    private List<File> locations;



    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public List<File> getLocations() {
        return locations;
    }

    public void setLocations(List<File> locations) {
        this.locations = locations;
    }

    public CodeExplorerConfiguration(String projectName, List<File> locations) {
        this.projectName = projectName;
        this.locations = locations;
    }

    public String serialize() {
        String locationsString = locations.stream()
                .map(File::getPath)
                .collect(Collectors.joining(PROJECT_PART_DELIMITER));
        return projectName + PROJECT_PART_DELIMITER + locationsString;
    }

    public static CodeExplorerConfiguration deserialize(String serialized) {
        String[] parts = serialized.split(PROJECT_PART_DELIMITER);
        String projectName = parts[0];
        List<File> locations = Arrays.stream(parts).skip(1)
                .map(File::new)
                .collect(Collectors.toCollection(ArrayList::new));
        return new CodeExplorerConfiguration(projectName, locations);
    }

    public JavaPackage importPackages() {
        Collection<Location> locations = getLocations().stream()
                .map(File::toURI)
                .map(Location::of)
                .collect(Collectors.toList());
        ClassFileImporter classFileImporter = new ClassFileImporter();
        JavaClasses  importedClasses = classFileImporter.importLocations(locations);
        JavaPackage rootPackage = importedClasses.getDefaultPackage();
        while(rootPackage.getClasses().isEmpty() && rootPackage.getSubpackages().size() == 1)
            rootPackage = rootPackage.getSubpackages().iterator().next();
        return rootPackage;
    }
}