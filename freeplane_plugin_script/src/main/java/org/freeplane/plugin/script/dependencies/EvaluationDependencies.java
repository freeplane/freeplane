package org.freeplane.plugin.script.dependencies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

public class EvaluationDependencies implements IExtension{

	static class DependentNodeReferences implements Iterable<NodeModel>{
		final private WeakHashMap<NodeModel, Void> references = new WeakHashMap<>();
		void add(NodeModel node) {
			references.put(node, null);
		}

		@Override
		public Iterator<NodeModel> iterator() {
			return references.keySet().iterator();
		}
	}

	enum Access {
		NODE, BRANCH, ALL
	}


	public static EvaluationDependencies of(MapModel map) {
		EvaluationDependencies dependencies = map.getExtension(EvaluationDependencies.class);
		if (dependencies == null) {
			dependencies = new EvaluationDependencies();
			map.addExtension(dependencies);
		}
		return dependencies;
	}

	private final WeakHashMap<MapModel, DependentNodeReferences> onMapDependencies = new WeakHashMap<>();

	private final WeakHashMap<NodeModel, DependentNodeReferences> onNodeDependencies = new WeakHashMap<>();
	// FIXME: organize node and branch dependencies in a tree?
	private final WeakHashMap<NodeModel, DependentNodeReferences> onBranchDependencies = new WeakHashMap<>();
	private final WeakHashMap<NodeModel, Void> onAnyNodeDependencies = new WeakHashMap<>();
	private final WeakHashMap<NodeModel, Void> onGlobalNodeDependencies = new WeakHashMap<>();

	public void getChangedDependencies(Set<NodeModel> accessingNodes, final NodeModel accessedNode) {
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
		ArrayList<NodeModel> onAnyNodeDependendingNodes = new ArrayList<>(onAnyNodeDependencies.keySet());
		onAnyNodeDependencies.clear();
        getRecursively(accessingNodes, onAnyNodeDependendingNodes);
//		System.out.println("dependencies on(" + node + "): " + accessingNodes);
	}

	public void getGlobalDependencies(Set<NodeModel> accessingNodes) {
		getRecursively(accessingNodes, onGlobalNodeDependencies.keySet());
//		System.out.println("dependencies on(" + node + "): " + accessingNodes);
	}

	public void removeAndReturnChangedDependencies(Set<NodeModel> accessingNodes, final MapModel accessedMap) {
		final Iterable<NodeModel> onMap = onMapDependencies.remove(accessedMap);
		if (onMap != null)
			getRecursively(accessingNodes, onMap);
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
	public void accessNode(NodeModel accessingNode, NodeModel accessedNode) {
		// FIXME: check if accessedNode is already covered by other accessModes
		provideDependencySet(accessedNode, onNodeDependencies).add(accessingNode);
		addAccessedMap(accessingNode, accessedNode);
//		System.out.println(accessingNode + " accesses " + accessedNode + ". current dependencies:\n" + this);
	}

	/** accessedNode.children was accessed when accessingNode was evaluated. */
	public void accessBranch(NodeModel accessingNode, NodeModel accessedNode) {
		// FIXME: check if accessedNode is already covered by other accessModes
		provideDependencySet(accessedNode, onBranchDependencies).add(accessingNode);
		addAccessedMap(accessingNode, accessedNode);
//		System.out.println(accessingNode + " accesses branch of " + accessedNode + ". current dependencies:\n" + this);
	}

	private void addAccessedMap(NodeModel accessingNode, NodeModel accessedNode) {
		final MapModel accessedMap = accessedNode.getMap();
		if(accessedMap != accessingNode.getMap())
			provideDependencySet(accessedMap, onMapDependencies).add(accessingNode);
	}

	/** a method was used on the accessingNode that may use any node in the map. */
	public void accessAll(NodeModel accessingNode) {
		// FIXME: check if accessedNode is already covered by other accessModes
		onAnyNodeDependencies.put(accessingNode, null);
//		System.out.println(accessingNode + " accesses all nodes. current dependencies:\n" + this);
	}

	public void accessGlobalNode(NodeModel accessingNode) {
		onGlobalNodeDependencies.put(accessingNode, null);
	}

	private <T>DependentNodeReferences provideDependencySet(final T accessed,
														 final WeakHashMap<T, DependentNodeReferences> dependenciesMap) {
		DependentNodeReferences set = dependenciesMap.get(accessed);
		if (set == null) {
			set = new DependentNodeReferences();
			dependenciesMap.put(accessed, set);
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
