/*
 * Created on 9 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.features.map.codeexplorermode;

import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.map.NodeModel;

class CodeConnectorModel extends ConnectorModel {

    private final int weight;
    CodeConnectorModel(NodeModel source, String targetID, int weight) {
        super(source, targetID);
        this.weight = weight;
    }
    public int weight() {
        return weight;
    }

}
