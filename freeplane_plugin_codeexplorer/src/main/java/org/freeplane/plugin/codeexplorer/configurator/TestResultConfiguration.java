/*
 * Created on 8 Feb 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.configurator;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.codeexplorer.dependencies.DependencyVerdict;
import org.freeplane.plugin.codeexplorer.map.CodeNode;
import org.freeplane.plugin.codeexplorer.task.AnnotationMatcher;
import org.freeplane.plugin.codeexplorer.task.CodeExplorerConfiguration;
import org.freeplane.plugin.codeexplorer.task.DependencyJudge;
import org.freeplane.plugin.codeexplorer.task.LocationMatcher;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.Source;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.freeplane.extension.ArchTestResult;

public class TestResultConfiguration implements CodeExplorerConfiguration {

    private final ArchTestResult testResult;

    public TestResultConfiguration(ArchTestResult testResult) {
        this.testResult = testResult;
    }

    @Override
    public int countLocations() {
        return testResult.violatingClassLocations.size();
    }

    @Override
    public JavaClasses importClasses() {
        final List<Location> classLocations = testResult.violatingClassLocations.values().stream()
                .flatMap(Set::stream)
                .map(x -> {
                    try {
                        return new URI(x);
                    } catch (URISyntaxException e) {
                        throw new IllegalArgumentException(e);
                    }
                })
                .map(Location::of)
                .collect(Collectors.toList());
        LogUtils.info("Starting import of " + countLocations() + " classes");
        final JavaClasses importedClasses = new ClassFileImporter().importLocations(classLocations);
        LogUtils.info("Import done");
        return importedClasses;
    }

    @Override
    public String getProjectName() {
        return testResult.violatedRuleDescription;

    }

    @Override
    public LocationMatcher createLocationMatcher() {
        return this::location;
    }

    private Optional<String> location(JavaClass javaClass) {
        return javaClass.getSource()
        .map(Source::getUri)
        .map(URI::toString)
        .flatMap(uri -> testResult.violatingClassLocations.entrySet().stream()
                .filter(e -> e.getValue().contains(uri))
                .findAny())
        .map(Entry::getKey)
        .flatMap(s-> s.isEmpty() ? CodeNode.classSourceLocationOf(javaClass) : Optional.of(s));
    }

    @Override
    public DependencyJudge getDependencyJudge() {
        return (dependency, goesUp) -> testResult.violationDependencyDescriptions.contains(dependency.getDescription())
                ? DependencyVerdict.FORBIDDEN : DependencyVerdict.ALLOWED;
    }

    @Override
    public AnnotationMatcher getAnnotationMatcher() {
        return AnnotationMatcher.IGNORING_ALL;
    }

    @Override
    public boolean canBeSaved() {
        return false;
    }
}
