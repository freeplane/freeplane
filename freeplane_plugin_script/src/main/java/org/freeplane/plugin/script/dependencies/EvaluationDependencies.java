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
    private final WeakHashMap<NodeModel, DependentNodeReferences> onBranchDependencies = new WeakHashMap<>();
    private final WeakHashMap<NodeModel, DependentNodeReferences> onCloneDependencies = new WeakHashMap<>();
	private final WeakHashMap<NodeModel, Void> onAnyNodeDependencies = new WeakHashMap<>();
	private final WeakHashMap<NodeModel, Void> onGlobalNodeDependencies = new WeakHashMap<>();

	public void collectChangedDependencies(Set<NodeModel> accessingNodes, final NodeModel accessedNode) {
		final Iterable<NodeModel> onNode = onNodeDependencies.get(accessedNode);
		if (onNode != null)
			collectAccessingNodesRecursively(accessingNodes, onNode);
        for (Entry<NodeModel, DependentNodeReferences> entry : onBranchDependencies.entrySet()) {
            final NodeModel branchNode = entry.getKey();
            if (accessedNode.isDescendantOf(branchNode)) {
                collectAccessingNodesRecursively(accessingNodes, entry.getValue());
            }
        }
        for (Entry<NodeModel, DependentNodeReferences> entry : onCloneDependencies.entrySet()) {
            final NodeModel cloneNode = entry.getKey();
            if (cloneNode.allClones().contains(accessedNode)
                    || cloneNode.subtreeClones().toCollection().stream().anyMatch(t -> t.isDescendantOf(accessedNode))) {
                collectAccessingNodesRecursively(accessingNodes, entry.getValue());
            }
        }
		if(! onAnyNodeDependencies.isEmpty()) {
		    ArrayList<NodeModel> onAnyNodeDependendingNodes = new ArrayList<>(onAnyNodeDependencies.keySet());
		    onAnyNodeDependencies.clear();
		    collectAccessingNodesRecursively(accessingNodes, onAnyNodeDependendingNodes);
		}
	}

	public void collectGlobalNodeDependencies(Set<NodeModel> accessingNodes) {
		collectAccessingNodesRecursively(accessingNodes, onGlobalNodeDependencies.keySet());
	}

	public void removeAndReturnChangedDependencies(Set<NodeModel> accessingNodes, final MapModel accessedMap) {
		final Iterable<NodeModel> onMap = onMapDependencies.remove(accessedMap);
		if (onMap != null)
			collectAccessingNodesRecursively(accessingNodes, onMap);
	}
	private void collectAccessingNodesRecursively(Set<NodeModel> accessingNodes, final Iterable<NodeModel> changedAccessedNodes) {
		for (NodeModel node : changedAccessedNodes) {
			// avoid loops
			if (accessingNodes.add(node)) {
				collectChangedDependencies(accessingNodes, node);
			}
		}
	}

	/** accessedNode was accessed when accessingNode was evaluated. */
	public void accessNode(NodeModel accessingNode, NodeModel accessedNode) {
        onNodeDependencies.computeIfAbsent(accessedNode, x -> new DependentNodeReferences()).add(accessingNode);
		addAccessedMap(accessingNode, accessedNode);
	}

    /** accessedNode.children was accessed when accessingNode was evaluated. */
    public void accessBranch(NodeModel accessingNode, NodeModel accessedNode) {
        onBranchDependencies.computeIfAbsent(accessedNode, x -> new DependentNodeReferences()).add(accessingNode);
        addAccessedMap(accessingNode, accessedNode);
    }

    /** accessedNode.children was accessed when accessingNode was evaluated. */
    public void accessClones(NodeModel accessingNode, NodeModel accessedNode) {
        onCloneDependencies.computeIfAbsent(accessedNode, x -> new DependentNodeReferences()).add(accessingNode);
        addAccessedMap(accessingNode, accessedNode);
    }

	private void addAccessedMap(NodeModel accessingNode, NodeModel accessedNode) {
		final MapModel accessedMap = accessedNode.getMap();
		if(accessedMap != accessingNode.getMap())
			onMapDependencies.computeIfAbsent(accessedMap, x -> new DependentNodeReferences()).add(accessingNode);
	}

	/** a method was used on the accessingNode that may use any node in the map. */
	public void accessAll(NodeModel accessingNode) {
		onAnyNodeDependencies.put(accessingNode, null);
	}

	public void accessGlobalNode(NodeModel accessingNode) {
		onGlobalNodeDependencies.put(accessingNode, null);
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
