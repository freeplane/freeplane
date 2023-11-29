/*
 * Created on 25 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;

import org.freeplane.plugin.codeexplorer.dependencies.DependencyJudge;

public interface CodeExplorer {
    void explore(CodeExplorerConfiguration configuration);
    void setJudge(DependencyJudge judge);
}
