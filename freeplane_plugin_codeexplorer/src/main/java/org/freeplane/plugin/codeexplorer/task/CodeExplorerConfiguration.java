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

import com.google.gson.annotations.SerializedName;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.thirdparty.com.google.common.annotations.VisibleForTesting;

public class CodeExplorerConfiguration {

    @SerializedName("projectName")
    private String projectName;

    @SerializedName("locations")
    private List<File> projectLocations;

    @SerializedName(value="configurationRules", alternate={"dependencyJudgeRules"})
    private String configurationRules;

    transient private ParsedConfiguration parsedConfiguration;

    public CodeExplorerConfiguration() {
        this("", new ArrayList<>(), "");
    }

    @VisibleForTesting
    CodeExplorerConfiguration(String projectName, List<File> locations, String dependencyJudgeRules) {
        this.projectName = projectName;
        this.projectLocations = locations.stream()
                .map(File::getAbsolutePath)
                .map(File::new)
                .collect(Collectors.toList());
        setConfigurationRules("");
        if(! dependencyJudgeRules.isEmpty()) {
            try {
                setConfigurationRules(dependencyJudgeRules);
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
        return projectLocations;
    }

    public void setLocations(List<File> locations) {
        this.projectLocations = locations;
    }

    public void removeAllLocations() {
        this.projectLocations.clear();
    }

    public String getConfigurationRules() {
        return configurationRules;
    }

    public void setConfigurationRules(String configurationRules) {
        this.parsedConfiguration = new ParsedConfiguration(configurationRules);
        this.configurationRules = configurationRules;
    }

    public DependencyJudge getDependencyJudge() {
        return parsedConfiguration.judge();
    }

    public JavaClasses importClasses() {
        DirectoryMatcher directoryMatcher = createDirectoryMatcher();
        Collection<Location> locations =
                directoryMatcher.getImportedLocations().stream()
                .map(File::toURI)
                .map(Location::of)
                .collect(Collectors.toList());
        ClassFileImporter classFileImporter = new ClassFileImporter()
                .withImportOption(parsedConfiguration.importOption());
        LogUtils.info("Starting import from " + locations.size() + " locations");
        JavaClasses  importedClasses = classFileImporter.importLocations(locations);
        LogUtils.info("Import done");
        return importedClasses;
    }

    public DirectoryMatcher createDirectoryMatcher() {
        return parsedConfiguration.directoryMatcher(projectLocations);
    }

    public void addLocation(File file) {
        projectLocations.add(new File(file.getAbsolutePath()));
    }

    public void addLocation(String path) {
       addLocation(new File(path));
    }
}
