/*
 * Created on 25 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;

public interface CodeExplorer {
    void explore(CodeExplorerConfiguration configuration);
    void setProjectConfiguration(DependencyJudge judge, AnnotationMatcher annotationMatcher);
    void cancelAnalysis();
}
