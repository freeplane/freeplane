package org.freeplane.plugin.script.proxy;

import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.plugin.script.proxy.Proxy.Map;
import org.freeplane.plugin.script.proxy.Proxy.Node;

public class MapProxy extends AbstractProxy<MapModel> implements Map {


	public MapProxy(MapModel map, MModeController modeController) {
		super(map, modeController);
    }

	public Node node(String id) {
		final NodeModel node = getDelegate().getNodeForID(id);
		return node != null ? new NodeProxy(node,
			getModeController()) : null;
    }

	public Node getRootNode() {
		final NodeModel rootNode = getDelegate().getRootNode();
		return new NodeProxy(rootNode, getModeController());
	}

}
