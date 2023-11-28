/*
 * Created on 9 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.connectors;

import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.codeexplorer.dependencies.DependencyVerdict;

class CodeConnectorModel extends ConnectorModel {

    private final int weight;
    private final boolean goesUp;
    private final DependencyVerdict dependencyVerdict;
    CodeConnectorModel(NodeModel source, String targetID, int weight, DependencyVerdict dependencyVerdict, boolean goesUp) {
        super(source, targetID);
        this.weight = weight;
        this.goesUp = goesUp;
        this.dependencyVerdict = dependencyVerdict;
    }

    int weight() {
        return weight;
    }

    boolean goesUp() {
        return goesUp;
    }

    public DependencyVerdict dependencyVerdict() {
        return dependencyVerdict;
    }
}
