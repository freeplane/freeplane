/*
 * Created on 25 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
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
    private Set<File> projectLocations;

    @SerializedName("userContent")
    private final Map<String, SortedSet<CodeNodeUserContent>> userContent;

    @SerializedName(value="configurationRules", alternate={"dependencyJudgeRules"})
    private String configurationRules;

    transient private ParsedConfiguration parsedConfiguration;

    @SerializedName("attributeConfiguration")
    private CodeAttributeConfiguration attributeConfiguration;

    public CodeExplorerConfiguration() {
        this("", new ArrayList<>(), "");
    }

    @VisibleForTesting
    CodeExplorerConfiguration(String projectName, List<File> locations, String dependencyJudgeRules) {
        this.projectName = projectName;
        this.projectLocations = locations.stream()
                .map(File::getAbsolutePath)
                .map(File::new)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        applyConfigurationRules("");
        if(! dependencyJudgeRules.isEmpty()) {
            try {
                applyConfigurationRules(dependencyJudgeRules);
            } catch (IllegalArgumentException e) {
                // silently ignore bad rules
            }
        }
        userContent = new TreeMap<>();
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void removeAllLocations() {
        this.projectLocations.clear();
    }

    public String getConfigurationRules() {
        return configurationRules;
    }


    void initialize() {
        try {
            userContent.values().stream().flatMap(Set::stream).forEach(CodeNodeUserContent::initialize);
            applyConfigurationRules(configurationRules);
            if(attributeConfiguration == null)
                attributeConfiguration = new CodeAttributeConfiguration();
            attributeConfiguration.initialize();
        } catch (Exception e) {
            configurationRules = "";
        }
    }
    public ConfigurationChange applyConfigurationRules(String configurationRules) {
        ParsedConfiguration newConfiguration = new ParsedConfiguration(configurationRules);
        ConfigurationChange status = newConfiguration.configurationChange(parsedConfiguration);
        this.configurationRules = configurationRules;
        parsedConfiguration = newConfiguration;
        return status;
    }

    public DependencyJudge getDependencyJudge() {
        return parsedConfiguration.judge();
    }

    public AnnotationMatcher getAnnotationMatcher() {
        return parsedConfiguration.annotationMatcher();
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

    public int countLocations() {
        return projectLocations.size();
    }

    public boolean containsLocation(String path) {
        return projectLocations.contains(new File(path).getAbsoluteFile());
    }

    public boolean removeLocation(String path) {
        return projectLocations.remove(new File(path).getAbsoluteFile());
    }

    public Collection<File> getLocations() {
        return projectLocations;
    }

    public void removeUserContent() {
        this.userContent.clear();
    }

    public void addUserContent(String location, CodeNodeUserContent content) {
        userContent.computeIfAbsent(location, x -> new TreeSet<>()).add(content);
    }

    public Map<String, SortedSet<CodeNodeUserContent>> getUserContent() {
        return userContent;
    }

    public CodeAttributeConfiguration getAttributeConfiguration() {
        return attributeConfiguration;
    }
}
