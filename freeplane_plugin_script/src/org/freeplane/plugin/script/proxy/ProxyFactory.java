package org.freeplane.plugin.script.proxy;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.model.NodeModel;
import org.freeplane.features.mindmapmode.MModeController;
public class ProxyFactory {
	static Proxy.Node createNode(final NodeModel node, final MModeController modeController){
		return new NodeProxy(node, modeController);
	}
	
	static Proxy.Controller createController(Controller controller){
		return new ControllerProxy(controller);
	}
}
