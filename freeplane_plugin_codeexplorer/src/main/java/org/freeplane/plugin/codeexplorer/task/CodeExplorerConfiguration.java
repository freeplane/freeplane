/*
 * Created on 25 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.freeplane.plugin.codeexplorer.dependencies.DependencyJudge;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.Location;

public class CodeExplorerConfiguration {

    private String projectName;
    private List<File> locations;
    private String dependencyJudgeRules;

    transient private DependencyJudge judge;

    public CodeExplorerConfiguration() {
        this("", new ArrayList<>(), "");
    }

    public CodeExplorerConfiguration(String projectName, List<File> locations, String dependencyJudgeRules) {
        this.projectName = projectName;
        this.locations = locations.stream()
                .map(File::getAbsoluteFile)
                .collect(Collectors.toList());
        setDependencyJudgeRules("");
        if(! dependencyJudgeRules.isEmpty()) {
            try {
                setDependencyJudgeRules(dependencyJudgeRules);
            } catch (IllegalArgumentException e) {
                // silently ignore bad rules
            }
        }
    }

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

    @JsonIgnore
    public DependencyJudge getDependencyJudge() {
        return judge;
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
}