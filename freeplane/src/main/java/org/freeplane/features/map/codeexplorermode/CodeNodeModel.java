/*
 * Created on 9 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.features.map.codeexplorermode;

import java.util.Collection;

import org.freeplane.core.extension.Configurable;
import org.freeplane.features.link.NodeLinkModel;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

abstract class CodeNodeModel extends NodeModel{

    CodeNodeModel(MapModel map) {
        super(map);
    }

    abstract Collection<? extends NodeLinkModel> getOutgoingLinks(Configurable component);
}
