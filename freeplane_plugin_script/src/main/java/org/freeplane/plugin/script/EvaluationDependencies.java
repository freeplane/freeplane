package org.freeplane.plugin.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

class EvaluationDependencies implements IExtension{

	static class DependentNodeReferences implements Iterable<NodeModel>{
		final private WeakHashMap<NodeModel, Void> references = new WeakHashMap<>();
		void add(NodeModel node) {
			references.put(node, null);
		}

		@Override
		public Iterator<NodeModel> iterator() {
			return references.keySet().iterator();
		}}

	enum Access {
		NODE, BRANCH, ALL
	}


	static EvaluationDependencies of(MapModel map) {
		EvaluationDependencies dependencies = map.getExtension(EvaluationDependencies.class);
		if (dependencies == null) {
			dependencies = new EvaluationDependencies();
			map.addExtension(dependencies);
		}
		return dependencies;
	}

	static IExtension removeFrom(MapModel map) {
		return map.removeExtension(EvaluationDependencies.class);
	}


	private final WeakHashMap<MapModel, Void> referencedMaps = new WeakHashMap<>();

	private final WeakHashMap<NodeModel, DependentNodeReferences> onNodeDependencies = new WeakHashMap<>();
	// FIXME: organize node and branch dependencies in a tree?
	private final WeakHashMap<NodeModel, DependentNodeReferences> onBranchDependencies = new WeakHashMap<>();
	private final WeakHashMap<NodeModel, Void> onAnyNodeDependencies = new WeakHashMap<>();
	private final WeakHashMap<NodeModel, Void> onGlobalNodeDependencies = new WeakHashMap<>();

	void getChangedDependencies(Set<NodeModel> accessingNodes, final NodeModel accessedNode) {
		final Iterable<NodeModel> onNode = onNodeDependencies.get(accessedNode);
		if (onNode != null)
			getRecursively(accessingNodes, onNode);
		ArrayList<Entry<NodeModel, DependentNodeReferences>> onBranchDependencyList = new ArrayList<>(onBranchDependencies.entrySet());
		for (Entry<NodeModel, DependentNodeReferences> entry : onBranchDependencyList) {
			final NodeModel branchNode = entry.getKey();
			if (accessedNode.isDescendantOf(branchNode)) {
				onBranchDependencies.get(branchNode);
				getRecursively(accessingNodes, entry.getValue());
			}
		}
		getRecursively(accessingNodes, onAnyNodeDependencies.keySet());
		onAnyNodeDependencies.clear();
//		System.out.println("dependencies on(" + node + "): " + accessingNodes);
	}

	void getGlobalDependencies(Set<NodeModel> accessingNodes) {
		getRecursively(accessingNodes, onGlobalNodeDependencies.keySet());
//		System.out.println("dependencies on(" + node + "): " + accessingNodes);
	}

	private void getRecursively(Set<NodeModel> accessingNodes, final Iterable<NodeModel> changedAccessedNodes) {
		for (NodeModel node : changedAccessedNodes) {
			// avoid loops
			if (accessingNodes.add(node)) {
				getChangedDependencies(accessingNodes, node);
			}
		}
	}

	/** accessedNode was accessed when accessingNode was evaluated. */
	void accessNode(NodeModel accessingNode, NodeModel accessedNode) {
		// FIXME: check if accessedNode is already covered by other accessModes
		provideDependencySet(accessedNode, onNodeDependencies).add(accessingNode);
		addAccessedMap(accessingNode, accessedNode);
//		System.out.println(accessingNode + " accesses " + accessedNode + ". current dependencies:\n" + this);
	}

	/** accessedNode.children was accessed when accessingNode was evaluated. */
	void accessBranch(NodeModel accessingNode, NodeModel accessedNode) {
		// FIXME: check if accessedNode is already covered by other accessModes
		provideDependencySet(accessedNode, onBranchDependencies).add(accessingNode);
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
		referencedMaps.put(accessedMap, null);
	}

	/** a method was used on the accessingNode that may use any node in the map. */
	void accessAll(NodeModel accessingNode) {
		// FIXME: check if accessedNode is already covered by other accessModes
		onAnyNodeDependencies.put(accessingNode, null);
//		System.out.println(accessingNode + " accesses all nodes. current dependencies:\n" + this);
	}

	void accessGlobalNode(NodeModel accessingNode) {
		onGlobalNodeDependencies.put(accessingNode, null);
	}

	private DependentNodeReferences provideDependencySet(final NodeModel accessedNode,
														 final WeakHashMap<NodeModel, DependentNodeReferences> dependenciesMap) {
		DependentNodeReferences set = dependenciesMap.get(accessedNode);
		if (set == null) {
			set = new DependentNodeReferences();
			dependenciesMap.put(accessedNode, set);
		}
		return set;
	}

	public Iterable<NodeModel> getPossibleDependencies(NodeModel node) {
		Iterable<NodeModel> dependencies = onNodeDependencies.get(node);
		return dependencies != null ? dependencies : Collections.<NodeModel>emptyList();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Entry<NodeModel, DependentNodeReferences> entry : onNodeDependencies.entrySet()) {
			builder.append("onNode (" + entry.getKey().getText() + "):\n");
			for (NodeModel nodeModel : entry.getValue()) {
				builder.append("  " + nodeModel + "\n");
			}
		}
		for (Entry<NodeModel, DependentNodeReferences> entry : onBranchDependencies.entrySet()) {
			builder.append("onBranch (" + entry.getKey().getText() + "):\n");
			for (NodeModel nodeModel : entry.getValue()) {
				builder.append("  " + nodeModel + "\n");
			}
		}
		if (!onAnyNodeDependencies.isEmpty()) {
			builder.append("onAnyNode:\n");
			for (NodeModel nodeModel : onAnyNodeDependencies.keySet()) {
				builder.append("  " + nodeModel + "\n");
			}
		}
		return builder.toString();
	}
}
