/*
 * Created on 28 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.dependencies;

import java.util.Objects;
import java.util.Optional;

import org.freeplane.plugin.codeexplorer.map.CodeNode;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.PackageMatcher;

public class DependencyRule {
    private final DependencyVerdict verdict;
    private final PackageMatcher originMatcher;
    private final PackageMatcher targetMatcher;
    private final DependencyDirection dependencyDirection;
    private final String targetPattern;
    private final String originPattern;

    public DependencyRule(DependencyVerdict type, String originPattern, String targetPattern, DependencyDirection dependencyDirection) {
        this.verdict = type;
        this.originPattern = originPattern;
        this.targetPattern = targetPattern;
        this.originMatcher = PackageMatcher.of(originPattern);
        this.targetMatcher = PackageMatcher.of(targetPattern);
        this.dependencyDirection = dependencyDirection;
    }

    public Optional<DependencyVerdict> match(Dependency dependency, boolean goesUp) {
        return this.originMatcher.matches(CodeNode.findEnclosingNamedClass(dependency.getOriginClass()).getName()) &&
               this.targetMatcher.matches(CodeNode.findEnclosingNamedClass(dependency.getTargetClass()).getName()) &&
               this.dependencyDirection.matches(goesUp) ? Optional.of(verdict): Optional.empty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependencyDirection, originPattern, targetPattern, verdict);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DependencyRule other = (DependencyRule) obj;
        return dependencyDirection == other.dependencyDirection && Objects.equals(originPattern,
                other.originPattern) && Objects.equals(targetPattern, other.targetPattern)
                && verdict == other.verdict;
    }


}