package org.freeplane.plugin.script.dependencies;

import org.freeplane.core.util.Pair;
import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.FormulaUtils;

class PrecedentsSearchStrategy implements DependencySearchStrategy {
	@Override
	public RelatedElements find(final NodeModel node) {
		final Object object = node.getUserObject();
		return getRelatedElements(node, object);
	}

	@Override
	public RelatedElements find(final NodeModel node, final Attribute attribute) {
		final Object object = attribute.getValue();
		return getRelatedElements(node, object);
	}

	private RelatedElements getRelatedElements(NodeModel node, Object object) {
		updateFormulaCache(node, object);
		return FormulaUtils.getRelatedElements(node, object);
	}

	private void updateFormulaCache(NodeModel node, Object object) {
		FormulaUtils.evaluateObject(node, object);
	}

	@Override
	public Pair<NodeModel, NodeModel> inConnectionOrder(Pair<NodeModel, NodeModel> nodePair) {
		return nodePair.swap();
	}

}
