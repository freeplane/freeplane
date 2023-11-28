/*
 * Created on 28 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.map;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeRelativePath;
import org.freeplane.plugin.codeexplorer.dependencies.CodeDependency;

import com.tngtech.archunit.core.domain.Dependency;

public class DependencyFactory {
    private final DependencySelection dependencySelection;

    public DependencyFactory(DependencySelection dependencySelection) {
        this.dependencySelection = dependencySelection;
    }

}
