package org.freeplane.plugin.script.proxy;

import groovy.lang.Closure;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.freeplane.core.filter.condition.ICondition;
import org.freeplane.core.model.NodeModel;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.plugin.script.proxy.Proxy.Node;

public class ProxyUtils {
	static List<Node> createNodeList(final List<NodeModel> list, final MModeController modeController) {
		return new AbstractList<Node>() {
			final private List<NodeModel> nodeModels = list;

			@Override
			public Node get(final int index) {
				final NodeModel nodeModel = nodeModels.get(index);
				return new NodeProxy(nodeModel, modeController);
			}

			@Override
			public int size() {
				return nodeModels.size();
			}
		};
	}

	static List<Node> find(ICondition condition, MModeController modeController, NodeModel nodeModel) {
		return createNodeList(findImpl(condition, nodeModel), modeController);
	}

	static List<Node> find(final Closure closure, final MModeController modeController, NodeModel nodeModel) {
		final ICondition condition = new ICondition() {
			public boolean checkNode(NodeModel node) {
				try {
					final Object result = closure.call(new Object[] { new NodeProxy(node, modeController) });
					if (result == null)
						throw new RuntimeException("find(): closure returned null instead of boolean/Boolean");
					return (Boolean) result;
				}
				catch (ClassCastException e) {
					throw new RuntimeException("find(): closure returned " + e.getMessage()
					        + " instead of boolean/Boolean");
				}
			}
		};
		return find(condition, modeController, nodeModel);
	}

	/** finds from any node downwards. */
	@SuppressWarnings("unchecked")
	private static List<NodeModel> findImpl(ICondition condition, NodeModel node) {
		// a shortcut for non-matching leaves
		if (node.isLeaf() && !condition.checkNode(node))
			return Collections.EMPTY_LIST;
		List<NodeModel> matches = new ArrayList<NodeModel>();
		if (condition.checkNode(node))
			matches.add(node);
		final Enumeration<NodeModel> children = node.children();
		while (children.hasMoreElements()) {
			final NodeModel child = children.nextElement();
			matches.addAll(findImpl(condition, child));
		}
		return matches;
	}
}
