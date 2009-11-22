package org.freeplane.plugin.script.proxy;

import org.freeplane.core.model.NodeModel;
import org.freeplane.features.mindmapmode.MModeController;

public class ProxyFactory {
	static Proxy.Controller createController(
			final MModeController modeController) {
		return new ControllerProxy(modeController);
	}

	static Proxy.Node createNode(final NodeModel node,
			final MModeController modeController) {
		return new NodeProxy(node, modeController);
	}
}
