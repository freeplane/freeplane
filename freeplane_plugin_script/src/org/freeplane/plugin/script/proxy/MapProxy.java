package org.freeplane.plugin.script.proxy;

import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.plugin.script.proxy.Proxy.Map;
import org.freeplane.plugin.script.proxy.Proxy.Node;

public class MapProxy extends AbstractProxy<MapModel> implements Map {
	public MapProxy(final MapModel map, final MModeController modeController) {
		super(map, modeController);
	}

	public Node node(final String id) {
		final NodeModel node = getDelegate().getNodeForID(id);
		return node != null ? new NodeProxy(node, getModeController()) : null;
	}

	public Node getRootNode() {
		final NodeModel rootNode = getDelegate().getRootNode();
		return new NodeProxy(rootNode, getModeController());
	}
}
