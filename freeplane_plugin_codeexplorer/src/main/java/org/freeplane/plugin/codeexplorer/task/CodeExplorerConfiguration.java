/*
 * Created on 25 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.freeplane.plugin.codeexplorer.dependencies.DependencyJudge;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.Location;

public class CodeExplorerConfiguration {
    public static CodeExplorerConfiguration deserialize(String serialized) {
        String[] parts = serialized.split(PROJECT_PART_DELIMITER);
        String projectName = parts[0];
        String rules = parts.length > 1 ? parts[parts.length - 1] : "";
        List<File> locations = Arrays.stream(parts)
                .limit(parts.length - 1)
                .skip(1)
                .map(File::new)
                .collect(Collectors.toCollection(ArrayList::new));
        if(! rules.contains("->")) {
            File file = new File(rules);
            if (file.exists()) {
                locations.add(file);
                rules = "";
            }
        }
        return new CodeExplorerConfiguration(projectName, locations, rules.replace(';', '\n'));
    }

    private static final String PROJECT_PART_DELIMITER = "\t";
    private String projectName;
    private List<File> locations;
    private String dependencyJudgeRules;
    private DependencyJudge judge;



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



    public String getDependencyJudgeRules() {
        return dependencyJudgeRules;
    }

    public void setDependencyJudgeRules(String dependencyJudgeRules) {
        this.judge = DependencyJudge.of(dependencyJudgeRules);
        this.dependencyJudgeRules = dependencyJudgeRules;
    }

    public CodeExplorerConfiguration(String projectName, List<File> locations, String dependencyJudgeRules) {
        this.projectName = projectName;
        this.locations = locations;
        setDependencyJudgeRules("");
        setDependencyJudgeRules(dependencyJudgeRules);
    }

    public String serialize() {
        String locationsString = locations.stream()
                .map(File::getPath)
                .collect(Collectors.joining(PROJECT_PART_DELIMITER));
        return projectName + PROJECT_PART_DELIMITER + locationsString + PROJECT_PART_DELIMITER
                + dependencyJudgeRules.replace('\n', ';').replace('\t', ' ');
    }

    public JavaPackage importPackages() {
        Collection<Location> locations = getLocations().stream()
                .map(this::findClasses)
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

    private File findClasses(File file) {
        if(!file.isDirectory())
            return file;
        File mavenTargetClasses = new File(file, "target/classes");
        return mavenTargetClasses.isDirectory() ? mavenTargetClasses : file;
    }

    public DependencyJudge getDependencyJudge() {
        return judge;
    }
}