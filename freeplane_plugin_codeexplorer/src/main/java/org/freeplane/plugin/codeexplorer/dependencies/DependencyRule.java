/*
 * Created on 28 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.dependencies;

import org.freeplane.plugin.codeexplorer.map.CodeNodeModel;

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
        return this.originMatcher.matches(CodeNodeModel.findEnclosingNamedClass(dependency.getOriginClass()).getName()) &&
               this.targetMatcher.matches(CodeNodeModel.findEnclosingNamedClass(dependency.getTargetClass()).getName()) &&
               this.dependencyDirection.matches(goesUp);
    }
}