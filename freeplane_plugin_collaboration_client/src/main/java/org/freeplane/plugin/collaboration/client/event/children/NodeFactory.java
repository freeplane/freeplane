package org.freeplane.plugin.collaboration.client.event.children;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

class NodeFactory {

	public NodeModel createNode(MapModel map, String nodeId) {
		final NodeModel nodeModel = new NodeModel(map);
		nodeModel.setID(nodeId);
		return nodeModel;
	}
}
