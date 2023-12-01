/*
 * Created on 28 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.dependencies;

import org.freeplane.plugin.codeexplorer.map.CodeNode;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.PackageMatcher;

class DependencyRule {
    DependencyVerdict type;
    PackageMatcher originMatcher;
    PackageMatcher targetMatcher;
    DependencyDirection dependencyDirection;

    DependencyRule(DependencyVerdict type, PackageMatcher originMatcher, PackageMatcher targetMatcher, DependencyDirection dependencyDirection) {
        this.type = type;
        this.originMatcher = originMatcher;
        this.targetMatcher = targetMatcher;
        this.dependencyDirection = dependencyDirection;
    }

    boolean matches(Dependency dependency, boolean goesUp) {
        return this.originMatcher.matches(CodeNode.findEnclosingNamedClass(dependency.getOriginClass()).getName()) &&
               this.targetMatcher.matches(CodeNode.findEnclosingNamedClass(dependency.getTargetClass()).getName()) &&
               this.dependencyDirection.matches(goesUp);
    }
}