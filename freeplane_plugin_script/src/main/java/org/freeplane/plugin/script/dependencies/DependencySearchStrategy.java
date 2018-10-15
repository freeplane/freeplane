package org.freeplane.plugin.script.dependencies;

import org.freeplane.core.util.Pair;
import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.map.NodeModel;

public interface DependencySearchStrategy {
	RelatedElements find(NodeModel node);

	RelatedElements find(NodeModel node, Attribute attribute);

	Pair<NodeModel, NodeModel> inConnectionOrder(Pair<NodeModel, NodeModel> nodePair);

	public static final DependencySearchStrategy PRECENDENTS = new PrecendentsSearchStrategy();
	public static final DependencySearchStrategy DEPENDENTS = new DependentsSearchStrategy();
}