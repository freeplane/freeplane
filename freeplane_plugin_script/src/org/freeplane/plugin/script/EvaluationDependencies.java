package org.freeplane.plugin.script;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;

public class EvaluationDependencies implements IExtension{
	public enum Access {
		NODE, BRANCH, ALL
	}

	private HashMap<NodeModel, HashSet<NodeModel>> onNodeDependencies = new HashMap<NodeModel, HashSet<NodeModel>>();
	// FIXME: organize node and branch dependencies in a tree?
	private HashMap<NodeModel, HashSet<NodeModel>> onBranchDependencies = new HashMap<NodeModel, HashSet<NodeModel>>();
	private HashSet<NodeModel> onAnyNodeDependencies = new HashSet<NodeModel>();

	public Set<NodeModel> getDependencies(Set<NodeModel> result, final NodeModel node) {
		final HashSet<NodeModel> onNode = onNodeDependencies.get(node);
		if (onNode != null)
			addRecursively(result, onNode);
		for (Entry<NodeModel, HashSet<NodeModel>> entry : onBranchDependencies.entrySet()) {
			if (node.isDescendantOf(entry.getKey()))
				addRecursively(result, entry.getValue());
		}
		addRecursively(result, onAnyNodeDependencies);
//		System.out.println("dependencies on(" + node + "): " + result);
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
	public void accessNode(NodeModel formulaNode, NodeModel accessedNode) {
		// FIXME: check if accessedNode is already covered by other accessModes
		getDependencySet(accessedNode, onNodeDependencies).add(formulaNode);
//		System.out.println(formulaNode + " accesses " + accessedNode + ". current dependencies:\n" + this);
	}

	/** accessedNode.children was accessed when formulaNode was evaluated. */
	public void accessBranch(NodeModel formulaNode, NodeModel accessedNode) {
		// FIXME: check if accessedNode is already covered by other accessModes
		getDependencySet(accessedNode, onBranchDependencies).add(formulaNode);
//		System.out.println(formulaNode + " accesses branch of " + accessedNode + ". current dependencies:\n" + this);
	}

	/** a method was used on the formulaNode that may use any node in the map. */
	public void accessAll(NodeModel formulaNode) {
		// FIXME: check if accessedNode is already covered by other accessModes
		onAnyNodeDependencies.add(formulaNode);
//		System.out.println(formulaNode + " accesses all nodes. current dependencies:\n" + this);
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
		for (Entry<NodeModel, HashSet<NodeModel>> entry : onBranchDependencies.entrySet()) {
			builder.append("onBranch (" + entry.getKey().getText() + "):\n");
			for (NodeModel nodeModel : entry.getValue()) {
				builder.append("  " + nodeModel + "\n");
			}
		}
		if (!onAnyNodeDependencies.isEmpty()) {
			builder.append("onAnyNode:\n");
			for (NodeModel nodeModel : onAnyNodeDependencies) {
				builder.append("  " + nodeModel + "\n");
			}
		}
		return builder.toString();
	}
}
