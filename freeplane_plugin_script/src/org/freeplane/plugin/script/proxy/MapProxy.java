package org.freeplane.plugin.script.proxy;

import java.io.File;

import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.plugin.script.proxy.Proxy.Map;
import org.freeplane.plugin.script.proxy.Proxy.Node;

public class MapProxy extends AbstractProxy<MapModel> implements Map {
	public MapProxy(final MapModel map) {
		super(map);
	}

	public Node node(final String id) {
		final NodeModel node = getDelegate().getNodeForID(id);
		return node != null ? new NodeProxy(node) : null;
	}

	public Node getRootNode() {
		final NodeModel rootNode = getDelegate().getRootNode();
		return new NodeProxy(rootNode);
	}

	public File getFile() {
		return getDelegate().getFile();
	}
}
