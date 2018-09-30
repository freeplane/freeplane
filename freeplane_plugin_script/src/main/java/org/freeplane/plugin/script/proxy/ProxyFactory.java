package org.freeplane.plugin.script.proxy;

import java.util.AbstractList;
import java.util.List;

import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.ScriptExecution;

public class ProxyFactory {
	static public Proxy.Controller createController(final ScriptExecution scriptExecution) {
		return new ControllerProxy(scriptExecution);
	}

	static public Proxy.Node createNode(final NodeModel node, final ScriptExecution scriptExecution) {
		return new NodeProxy(node, scriptExecution);
	}

	static List<Proxy.Node> createNodeList(final List<NodeModel> list, final ScriptExecution scriptExecution) {
    	return new AbstractList<Proxy.Node>() {
    		final private List<NodeModel> nodeModels = list;
    
    		@Override
    		public Proxy.Node get(final int index) {
    			final NodeModel nodeModel = nodeModels.get(index);
    			return new NodeProxy(nodeModel, scriptExecution);
    		}
    
    		@Override
    		public int size() {
    			return nodeModels.size();
    		}
    	};
    }
}
