/*
 * Created on 25 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;

import com.tngtech.archunit.freeplane.extension.ArchTestResult;

public interface CodeExplorer {
    void explore(CodeExplorerConfiguration configuration);
    void setProjectConfiguration(DependencyJudge judge, AnnotationMatcher annotationMatcher);
    void cancelAnalysis();
}
