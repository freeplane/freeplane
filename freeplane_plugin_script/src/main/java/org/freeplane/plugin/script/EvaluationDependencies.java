package org.freeplane.plugin.script;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

public class EvaluationDependencies implements IExtension{

	static class DependantNodeReferences implements Iterable<NodeModel>{
		final private WeakHashMap<NodeModel, Void> references = new WeakHashMap<>();
		public void add(NodeModel node) {
			references.put(node, null);
		}

		@Override
		public Iterator<NodeModel> iterator() {
			return references.keySet().iterator();
		}}

	public enum Access {
		NODE, BRANCH, ALL
	}

	private final HashSet<MapModel> referencedMaps = new HashSet<>();

	private final HashMap<NodeModel, DependantNodeReferences> onNodeDependencies = new HashMap<>();
	// FIXME: organize node and branch dependencies in a tree?
	private final HashMap<NodeModel, DependantNodeReferences> onBranchDependencies = new HashMap<>();
	private final HashSet<NodeModel> onAnyNodeDependencies = new HashSet<NodeModel>();
	private final HashSet<NodeModel> onGlobalNodeDependencies = new HashSet<NodeModel>();

	public Set<NodeModel> getDependencies(Set<NodeModel> result, final NodeModel node) {
		final Iterable<NodeModel> onNode = onNodeDependencies.get(node);
		if (onNode != null)
			addRecursively(result, onNode);
		for (Entry<NodeModel, DependantNodeReferences> entry : onBranchDependencies.entrySet()) {
			if (node.isDescendantOf(entry.getKey()))
				addRecursively(result, entry.getValue());
		}
		addRecursively(result, onAnyNodeDependencies);
//		System.out.println("dependencies on(" + node + "): " + result);
		return result;
	}

	public Set<NodeModel> getGlobalDependencies(Set<NodeModel> result) {
		addRecursively(result, onGlobalNodeDependencies);
//		System.out.println("dependencies on(" + node + "): " + result);
		return result;
	}

	private void addRecursively(Set<NodeModel> dependentNodes, final Iterable<NodeModel> nodesToAdd) {
		for (NodeModel node : nodesToAdd) {
			// avoid loops
			if (dependentNodes.add(node))
				dependentNodes.addAll(getDependencies(dependentNodes, node));
		}
	}

	/** accessedNode was accessed when accessingNode was evaluated. */
	public void accessNode(NodeModel accessingNode, NodeModel accessedNode) {
		// FIXME: check if accessedNode is already covered by other accessModes
		getDependencySet(accessedNode, onNodeDependencies).add(accessingNode);
		addAccessedMap(accessingNode, accessedNode);
//		System.out.println(accessingNode + " accesses " + accessedNode + ". current dependencies:\n" + this);
	}

	/** accessedNode.children was accessed when accessingNode was evaluated. */
	public void accessBranch(NodeModel accessingNode, NodeModel accessedNode) {
		// FIXME: check if accessedNode is already covered by other accessModes
		getDependencySet(accessedNode, onBranchDependencies).add(accessingNode);
		addAccessedMap(accessingNode, accessedNode);
//		System.out.println(accessingNode + " accesses branch of " + accessedNode + ". current dependencies:\n" + this);
	}

	private void addAccessedMap(NodeModel accessingNode, NodeModel accessedNode) {
		final MapModel accessedMap = accessedNode.getMap();
		final MapModel accessingMap = accessingNode.getMap();
		if(! accessingMap.equals(accessedMap))
			EvaluationDependencies.of(accessingMap).addAccessedMap(accessedMap);
	}

	private void addAccessedMap(final MapModel accessedMap) {
		referencedMaps.add(accessedMap);
	}

	/** a method was used on the accessingNode that may use any node in the map. */
	public void accessAll(NodeModel accessingNode) {
		// FIXME: check if accessedNode is already covered by other accessModes
		onAnyNodeDependencies.add(accessingNode);
//		System.out.println(accessingNode + " accesses all nodes. current dependencies:\n" + this);
	}

	public void accessGlobalNode(NodeModel accessingNode) {
		onGlobalNodeDependencies.add(accessingNode);
	}

	private DependantNodeReferences getDependencySet(final NodeModel accessedNode,
	                                            final HashMap<NodeModel, DependantNodeReferences> dependenciesMap) {
		DependantNodeReferences set = dependenciesMap.get(accessedNode);
		if (set == null) {
			set = new DependantNodeReferences();
			dependenciesMap.put(accessedNode, set);
		}
		return set;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Entry<NodeModel, DependantNodeReferences> entry : onNodeDependencies.entrySet()) {
			builder.append("onNode (" + entry.getKey().getText() + "):\n");
			for (NodeModel nodeModel : entry.getValue()) {
				builder.append("  " + nodeModel + "\n");
			}
		}
		for (Entry<NodeModel, DependantNodeReferences> entry : onBranchDependencies.entrySet()) {
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

	static EvaluationDependencies of(MapModel map) {
		EvaluationDependencies dependencies = map.getExtension(EvaluationDependencies.class);
		if (dependencies == null) {
			dependencies = new EvaluationDependencies();
			map.addExtension(dependencies);
		}
		return dependencies;
	}

}
