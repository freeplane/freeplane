/*
 * Created on 8 Feb 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;

import org.freeplane.plugin.codeexplorer.dependencies.DependencyVerdict;

import com.tngtech.archunit.core.domain.Dependency;

public interface DependencyJudge {
    DependencyVerdict judge(Dependency dependency, boolean goesUp);
}
