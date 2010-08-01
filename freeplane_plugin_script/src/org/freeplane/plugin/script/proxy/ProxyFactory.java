package org.freeplane.plugin.script.proxy;

import java.util.AbstractList;
import java.util.List;

import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.mindmapmode.MModeController;

public class ProxyFactory {
	static public Proxy.Controller createController() {
		return new ControllerProxy();
	}

	static public Proxy.Node createNode(final NodeModel node) {
		return new NodeProxy(node);
	}

	static List<Proxy.Node> createNodeList(final List<NodeModel> list, final MModeController modeController) {
    	return new AbstractList<Proxy.Node>() {
    		final private List<NodeModel> nodeModels = list;
    
    		@Override
    		public Proxy.Node get(final int index) {
    			final NodeModel nodeModel = nodeModels.get(index);
    			return new NodeProxy(nodeModel);
    		}
    
    		@Override
    		public int size() {
    			return nodeModels.size();
    		}
    	};
    }
}
