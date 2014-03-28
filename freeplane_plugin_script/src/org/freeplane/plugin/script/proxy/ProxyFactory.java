package org.freeplane.plugin.script.proxy;

import java.util.AbstractList;
import java.util.Comparator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.ScriptContext;
import org.freeplane.plugin.script.proxy.Proxy.Node;

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

			@Override
			public void replaceAll(UnaryOperator<Node> operator) {
				// TODO Auto-generated method stub
			}

			@Override
			public void sort(Comparator<? super Node> c) {
				// TODO Auto-generated method stub
			}

			@Override
			public Spliterator<Node> spliterator() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean removeIf(Predicate<? super Node> filter) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public Stream<Node> stream() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Stream<Node> parallelStream() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void forEach(Consumer<? super Node> action) {
				// TODO Auto-generated method stub
			}
    	};
    }
}
