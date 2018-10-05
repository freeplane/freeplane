package org.freeplane.plugin.formula.dependencies;

import org.freeplane.core.util.Pair;
import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.FormulaUtils;
import org.freeplane.plugin.script.RelatedElements;

class PrependentsSearchStrategy implements FormulaDependencyTracer.DependencySearchStrategy {
	@Override
	public RelatedElements find(final NodeModel node) {
		return FormulaUtils.getRelatedElements(node, node.getUserObject());
	}

	@Override
	public RelatedElements find(final NodeModel node, final Attribute attribute) {
		return FormulaUtils.getRelatedElements(node, attribute.getValue());
	}


	@Override
	public Pair<NodeModel, NodeModel> inConnectionOrder(Pair<NodeModel, NodeModel> nodePair) {
		return nodePair.swap();
	}

}
