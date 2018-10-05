package org.freeplane.plugin.formula.dependencies;

import org.freeplane.core.util.Pair;
import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.FormulaDependencies;
import org.freeplane.plugin.script.FormulaUtils;
import org.freeplane.plugin.script.RelatedElements;

import java.util.Collection;
import java.util.Vector;

class DependentsSearchStrategy implements FormulaDependencyTracer.DependencySearchStrategy {
	@Override
	public RelatedElements find(final NodeModel node) {
		return find(node, node);
	}

	@Override
	public RelatedElements find(NodeModel node, Attribute attribute) {
		return find(node, attribute);
	}

	private RelatedElements find(final NodeModel node, Object element) {
		final Iterable<NodeModel> possibleDependencies = FormulaDependencies.getPossibleDependencies(node);
		final RelatedElements relatedElements = new RelatedElements(node);
		for (final NodeModel candidate : possibleDependencies) {
			final Object userObject = candidate.getUserObject();
			final Collection<Object> candidatePrecedents = FormulaUtils.getRelatedElements(candidate, userObject).getElements();
			if (candidatePrecedents.contains(node))
				relatedElements.relateNode(candidate);
			final Vector<Attribute> attributes = candidate.getExtension(NodeAttributeTableModel.class).getAttributes();
			attributes.stream().filter(a -> FormulaUtils.getRelatedElements(candidate, a.getValue()).getElements().contains(element))
					.forEach(a -> relatedElements.relateAttribute(candidate, a));
		}
		return relatedElements;
	}

	@Override
	public Pair<NodeModel, NodeModel> inConnectionOrder(Pair<NodeModel, NodeModel> nodePair) {
		return nodePair;
	}
}
