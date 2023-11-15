/*
 * Created on 9 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.main.codeexplorermode;

import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.map.NodeModel;

class CodeConnectorModel extends ConnectorModel {

    private final int weight;
    private final boolean goesUp;
    CodeConnectorModel(NodeModel source, String targetID, int weight, boolean goesUp) {
        super(source, targetID);
        this.weight = weight;
        this.goesUp = goesUp;
    }

    int weight() {
        return weight;
    }

    boolean goesUp() {
        return goesUp;
    }
}
