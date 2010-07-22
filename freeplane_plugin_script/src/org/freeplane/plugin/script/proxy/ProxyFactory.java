package org.freeplane.plugin.script.proxy;

import org.freeplane.features.common.map.NodeModel;

public class ProxyFactory {
	static public Proxy.Controller createController() {
		return new ControllerProxy();
	}

	static public Proxy.Node createNode(final NodeModel node) {
		return new NodeProxy(node);
	}
}
