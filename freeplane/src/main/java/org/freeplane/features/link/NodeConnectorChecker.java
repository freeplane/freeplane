package org.freeplane.features.link;

import java.util.Set;
import org.freeplane.features.map.NodeModel;

class NodeConnectorChecker {
    static boolean checkNodeConnectors(final NodeModel node, ConnectorChecker connectorChecker) {
        final NodeLinks nodeLinks = NodeLinks.getLinkExtension(node);
        if (nodeLinks != null) {
            for (final NodeLinkModel l : nodeLinks.getLinks()) {
                if (!(l instanceof ConnectorModel)) {
                    continue;
                }
                if (connectorChecker.check((ConnectorModel) l)) {
                    return true;
                }
            }
        }
        if (!node.hasID()) {
            return false;
        }
        final MapLinks mapLinks = MapLinks.getLinks(node.getMap());
        if (mapLinks == null) {
            return false;
        }
        final Set<NodeLinkModel> targetLinks = mapLinks.get(node.getID());
        if (targetLinks == null) {
            return false;
        }
        for (final NodeLinkModel l : targetLinks) {
            if (!(l instanceof ConnectorModel)) {
                continue;
            }
            if (connectorChecker.check((ConnectorModel) l)) {
                return true;
            }
        }
        return false;
    }

}
