package org.freeplane.features.link;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

class EvaluationDependencies implements IExtension{
	private static EvaluationDependencies getEvaluationDependencies(MapModel map) {
		EvaluationDependencies dependencies = (EvaluationDependencies) map.getExtension(EvaluationDependencies.class);
		if (dependencies == null) {
			dependencies = new EvaluationDependencies();
			map.addExtension(dependencies);
		}
		return dependencies;
	}

	public static List<NodeModel> manageChangeAndReturnDependencies(boolean includeChanged, final NodeModel... nodes) {
		final ArrayList<NodeModel> dependencies = new ArrayList<NodeModel>();
		for (int i = 0; i < nodes.length; i++) {
			final LinkedHashSet<NodeModel> nodeDependencies = new LinkedHashSet<NodeModel>(0);
			getEvaluationDependencies(nodes[i].getMap()).getDependencies(nodeDependencies, nodes[i]);
			if (nodeDependencies != null)
				dependencies.addAll(nodeDependencies);
			if (includeChanged)
				dependencies.add(nodes[i]);
		}
		return dependencies;
	}

	public static void accessNode(NodeModel accessingNode, NodeModel accessedNode) {
			getEvaluationDependencies(accessingNode.getMap()).accessNodeImpl(accessingNode, accessedNode);
	}
	
	private HashMap<NodeModel, HashSet<NodeModel>> onNodeDependencies = new HashMap<NodeModel, HashSet<NodeModel>>();

	public Set<NodeModel> getDependencies(Set<NodeModel> result, final NodeModel node) {
		final HashSet<NodeModel> onNode = onNodeDependencies.get(node);
		if (onNode != null)
			addRecursively(result, onNode);
		return result;
	}

	private void addRecursively(Set<NodeModel> dependentNodes, final HashSet<NodeModel> nodesToAdd) {
		for (NodeModel node : nodesToAdd) {
			// avoid loops
			if (dependentNodes.add(node))
				dependentNodes.addAll(getDependencies(dependentNodes, node));
		}
	}

	/** accessedNode was accessed when formulaNode was evaluated. */
	private void accessNodeImpl(NodeModel formulaNode, NodeModel accessedNode) {
		// FIXME: check if accessedNode is already covered by other accessModes
		getDependencySet(accessedNode, onNodeDependencies).add(formulaNode);
//		System.out.println(formulaNode + " accesses " + accessedNode + ". current dependencies:\n" + this);
	}

	private HashSet<NodeModel> getDependencySet(final NodeModel accessedNode,
	                                            final HashMap<NodeModel, HashSet<NodeModel>> dependenciesMap) {
		HashSet<NodeModel> set = dependenciesMap.get(accessedNode);
		if (set == null) {
			set = new HashSet<NodeModel>();
			dependenciesMap.put(accessedNode, set);
		}
		return set;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Entry<NodeModel, HashSet<NodeModel>> entry : onNodeDependencies.entrySet()) {
			builder.append("onNode (" + entry.getKey().getText() + "):\n");
			for (NodeModel nodeModel : entry.getValue()) {
				builder.append("  " + nodeModel + "\n");
			}
		}
		return builder.toString();
	}
}
