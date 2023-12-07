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

import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.codeexplorer.dependencies.DependencyJudge;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
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
                .map(File::getAbsolutePath)
                .map(File::new)
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

    public void removeAllLocations() {
        this.locations.clear();
    }

    public String getDependencyJudgeRules() {
        return dependencyJudgeRules;
    }

    public void setDependencyJudgeRules(String dependencyJudgeRules) {
        this.judge = DependencyJudge.of(dependencyJudgeRules);
        this.dependencyJudgeRules = dependencyJudgeRules;
    }

    public DependencyJudge getDependencyJudge() {
        return judge;
    }

    public JavaClasses importClasses() {
        Collection<Location> locations = getLocations().stream()
                .map(File::toURI)
                .map(Location::of)
                .collect(Collectors.toList());
        ClassFileImporter classFileImporter = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS);
        LogUtils.info("Starting import from " + locations.size() + " locations");
        JavaClasses  importedClasses = classFileImporter.importLocations(locations);
        LogUtils.info("Import done");
        return importedClasses;
    }

    public void addLocation(File file) {
        locations.add(new File(file.getAbsolutePath()));
    }

    public void addLocation(String path) {
       addLocation(new File(path));
    }
}
