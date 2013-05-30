package org.freeplane.plugin.script.proxy;

import java.util.AbstractList;
import java.util.List;

import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.ScriptContext;

public class ProxyFactory {
	static public Proxy.Controller createController(final ScriptContext scriptContext) {
		return new ControllerProxy(scriptContext);
	}

	static public Proxy.Node createNode(final NodeModel node, final ScriptContext scriptContext) {
		return new NodeProxy(node, scriptContext);
	}

	static List<Proxy.Node> createNodeList(final List<NodeModel> list, final ScriptContext scriptContext) {
    	return new AbstractList<Proxy.Node>() {
    		final private List<NodeModel> nodeModels = list;
    
    		@Override
    		public Proxy.Node get(final int index) {
    			final NodeModel nodeModel = nodeModels.get(index);
    			return new NodeProxy(nodeModel, scriptContext);
    		}
    
    		@Override
    		public int size() {
    			return nodeModels.size();
    		}
    	};
    }
}
