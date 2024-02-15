/*
 * Created on 8 Feb 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.archunit;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.codeexplorer.dependencies.DependencyVerdict;
import org.freeplane.plugin.codeexplorer.map.CodeNode;
import org.freeplane.plugin.codeexplorer.task.AnnotationMatcher;
import org.freeplane.plugin.codeexplorer.task.CodeExplorerConfiguration;
import org.freeplane.plugin.codeexplorer.task.DependencyJudge;
import org.freeplane.plugin.codeexplorer.task.LocationMatcher;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.Source;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.freeplane.extension.ArchitectureViolations;

public class ArchitectureViolationsConfiguration implements CodeExplorerConfiguration {

    private final ArchitectureViolations architectureViolations;
    private final Set<String> violationDependencyDescriptions;
    private JavaClasses importedClasses;
    private Map<String, Dependency> violations;

    public ArchitectureViolationsConfiguration(ArchitectureViolations architectureViolations) {
        this.architectureViolations = architectureViolations;
        this.violationDependencyDescriptions = architectureViolations.violationDescriptions.stream()
                .flatMap(x-> x.violationDependencyDescriptions.stream())
                .collect(Collectors.toSet());
    }

    @Override
    public int countLocations() {
        return architectureViolations.violatingClassLocations.size();
    }

    @Override
    public JavaClasses importClasses() {
        if(importedClasses == null) {
            final List<Location> classLocations = architectureViolations.violatingClassLocations.values().stream()
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
            importedClasses = new ClassFileImporter().importLocations(classLocations);
            LogUtils.info("Import done");
        }
        return importedClasses;
    }

    public Map<String, Dependency> violationsByRule(){
        if (violations == null) {
            importClasses();
            violations = importedClasses.stream()
            .flatMap(javaClass -> Stream.concat(javaClass.getDirectDependenciesFromSelf().stream(),
                    javaClass.getDirectDependenciesToSelf().stream()))
            .parallel()
            .filter(dependency-> violationDependencyDescriptions.contains(dependency.getDescription()))
            .collect(Collectors.toMap(Dependency::getDescription, x -> x, this::throwExceptionOnDifferentValues, HashMap::new));

        }
        return violations;
    }

    public Dependency throwExceptionOnDifferentValues(Dependency x, Dependency y) {
        if (x.equals(y))
            return x;
        else
            throw new IllegalArgumentException("Different dependencies with the same description " + x + " and " + y);
    }

    @Override
    public String getProjectName() {
        return architectureViolations.violatedRuleDescription;

    }

    @Override
    public LocationMatcher createLocationMatcher() {
        return this::location;
    }

    private Optional<String> location(JavaClass javaClass) {
        return javaClass.getSource()
        .map(Source::getUri)
        .map(URI::toString)
        .flatMap(uri -> architectureViolations.violatingClassLocations.entrySet().stream()
                .filter(e -> e.getValue().contains(uri))
                .findAny())
        .map(Entry::getKey)
        .flatMap(s-> s.isEmpty() ? CodeNode.classSourceLocationOf(javaClass) : Optional.of(s));
    }

    @Override
    public DependencyJudge getDependencyJudge() {
        return (dependency, goesUp) ->
        violationDependencyDescriptions.contains(dependency.getDescription())
        ? DependencyVerdict.FORBIDDEN
        : ( architectureViolations.isNoCyclesConditionChecked
             ? ( location(dependency.getOriginClass()).equals(location(dependency.getTargetClass()))
                        ? DependencyVerdict.IGNORED
                        : (goesUp ? DependencyVerdict.FORBIDDEN : DependencyVerdict.ALLOWED)
                  )
             : DependencyVerdict.IGNORED);

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
