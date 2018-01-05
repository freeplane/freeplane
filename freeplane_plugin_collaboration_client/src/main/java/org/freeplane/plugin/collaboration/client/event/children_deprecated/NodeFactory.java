package org.freeplane.plugin.collaboration.client.event.children_deprecated;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

public class NodeFactory {

	public NodeModel createNode(MapModel map) {
		final NodeModel nodeModel = new NodeModel(map);
		return nodeModel;
	}
}
