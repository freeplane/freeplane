package org.freeplane.plugin.script.proxy;

import groovy.lang.Closure;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import org.freeplane.features.common.filter.condition.ICondition;
import org.freeplane.features.common.format.IFormattedObject;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.plugin.script.ScriptContext;
import org.freeplane.plugin.script.proxy.Proxy.Node;

public class ProxyUtils {
	static List<Node> createNodeList(final List<NodeModel> list, final ScriptContext scriptContext) {
		return new AbstractList<Node>() {
			final private List<NodeModel> nodeModels = list;

			@Override
			public Node get(final int index) {
				final NodeModel nodeModel = nodeModels.get(index);
				return new NodeProxy(nodeModel, scriptContext);
			}

			@Override
			public int size() {
				return nodeModels.size();
			}
		};
	}

	static List<Node> find(final ICondition condition, final NodeModel node, final ScriptContext scriptContext) {
		return ProxyUtils.createNodeList(ProxyUtils.findImpl(condition, node, true), scriptContext);
	}
	
	static List<Node> findAll(final NodeModel node, final ScriptContext scriptContext, boolean breadthFirst) {
		return ProxyUtils.createNodeList(ProxyUtils.findImpl(null, node, breadthFirst), scriptContext);
	}

	static List<Node> find(final Closure closure, final NodeModel node, final ScriptContext scriptContext) {
		final ICondition condition = new ICondition() {
			public boolean checkNode(final NodeModel node) {
				try {
					final Object result = closure
					    .call(new Object[] { new NodeProxy(node, scriptContext) });
					if (result == null) {
						throw new RuntimeException("find(): closure returned null instead of boolean/Boolean");
					}
					return (Boolean) result;
				}
				catch (final ClassCastException e) {
					throw new RuntimeException("find(): closure returned " + e.getMessage()
					        + " instead of boolean/Boolean");
				}
			}
		};
		return ProxyUtils.find(condition, node, scriptContext);
	}

	/** finds from any node downwards.
	 * @param condition if null every node will match. */
	@SuppressWarnings("unchecked")
	private static List<NodeModel> findImpl(final ICondition condition, final NodeModel node, boolean breadthFirst) {
		final boolean nodeMatches = condition == null || condition.checkNode(node);
		// a shortcut for non-matching leaves
		if (node.isLeaf() && !nodeMatches) {
			return Collections.EMPTY_LIST;
		}
		final List<NodeModel> matches = new ArrayList<NodeModel>();
		if (nodeMatches && breadthFirst) {
			matches.add(node);
		}
		final Enumeration<NodeModel> children = node.children();
		while (children.hasMoreElements()) {
			final NodeModel child = children.nextElement();
			matches.addAll(ProxyUtils.findImpl(condition, child, breadthFirst));
		}
		if (nodeMatches && !breadthFirst) {
			matches.add(node);
		}
		return matches;
	}

	public static List<Proxy.Node> createListOfChildren(final NodeModel nodeModel, final ScriptContext scriptContext) {
        return new ArrayList<Proxy.Node>(new AbstractList<Proxy.Node>() {
    		@Override
    		public Proxy.Node get(final int index) {
    			final NodeModel child = (NodeModel) nodeModel.getChildAt(index);
    			return new NodeProxy(child, scriptContext);
    		}
    
    		@Override
    		public int size() {
    			return nodeModel.getChildCount();
    		}
    	});
    }

	/** this method is null-safe, i.e. value may be null and the result is not null. */
	public static Convertible attributeValueToConvertible(final NodeModel nodeModel, final ScriptContext scriptContext,
	                                             Object value) {
		if (value instanceof IFormattedObject)
			value = ((IFormattedObject) value).getObject();
		if (value instanceof Number)
			return new ConvertibleNumber((Number) value);
		else if (value instanceof Date)
			return new ConvertibleDate((Date) value);
		return new ConvertibleText(nodeModel, scriptContext, value == null ? null : value.toString());
	}

	public static Convertible nodeModelToConvertible(final NodeModel nodeModel, final ScriptContext scriptContext) {
        Object value = nodeModel.getUserObject();
    	if (value instanceof IFormattedObject)
    		value = ((IFormattedObject) value).getObject();
    	if (value instanceof Number)
    		return new ConvertibleNumber((Number) value);
    	else if (value instanceof Date)
    		return new ConvertibleDate((Date) value);
    	return new ConvertibleNodeText(nodeModel, scriptContext);
    }
}
