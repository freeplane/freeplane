package org.freeplane.plugin.script;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.dependencies.EvaluationDependencies;

public class FormulaDependencies{
	public static List<NodeModel> manageChangeAndReturnDependencies(boolean includeChanged, final NodeModel... changedNodes) {
		final ArrayList<NodeModel> dependencies = getAllChangedDependencies(includeChanged, changedNodes);
		FormulaCache.removeFromCache(dependencies);
		return dependencies;
	}

	public static void clearCache(final MapModel map) {
		FormulaCache.removeFrom(map);
		map.removeExtension(EvaluationDependencies.class);
	}


	private static ArrayList<NodeModel> getAllChangedDependencies(boolean includeChanged, final NodeModel... changedNodes) {
		final ArrayList<NodeModel> dependencies = new ArrayList<NodeModel>();
		for (int i = 0; i < changedNodes.length; i++) {
			final LinkedHashSet<NodeModel> accessingNodes = new LinkedHashSet<NodeModel>(0);
			EvaluationDependencies.of(changedNodes[i].getMap()).getChangedDependencies(accessingNodes, changedNodes[i]);
			if (accessingNodes != null)
				dependencies.addAll(accessingNodes);
			if (includeChanged)
				dependencies.add(changedNodes[i]);
		}
		return dependencies;
	}


	static void accessNode(NodeModel accessingNode, NodeModel accessedNode) {
		EvaluationDependencies.of(accessedNode.getMap()).accessNode(accessingNode, accessedNode);
	}

	static void accessBranch(NodeModel accessingNode, NodeModel accessedNode) {
		EvaluationDependencies.of(accessedNode.getMap()).accessBranch(accessingNode, accessedNode);
	}

	static void accessAll(NodeModel accessingNode) {
		EvaluationDependencies.of(accessingNode.getMap()).accessAll(accessingNode);
	}

	static void accessGlobalNode(NodeModel accessingNode) {
		EvaluationDependencies.of(accessingNode.getMap()).accessGlobalNode(accessingNode);
	}


	public static Iterable<NodeModel> getPossibleDependencies(NodeModel node) {
		return EvaluationDependencies.of(node.getMap()).getPossibleDependencies(node);
	}

	public static List<NodeModel> manageChangeAndReturnGlobalDependencies(MapModel map) {
		return manageChangeAndReturnDependencies(EvaluationDependencies.of(map)::getGlobalDependencies);
	}

	public static List<NodeModel> removeAndReturnMapDependencies(MapModel map) {
		return manageChangeAndReturnDependencies(
			set -> EvaluationDependencies.of(map).removeAndReturnChangedDependencies(set, map));
	}

	private static List<NodeModel> manageChangeAndReturnDependencies(Consumer<Set<NodeModel>> loader) {
		final LinkedHashSet<NodeModel> accessingNodes = new LinkedHashSet<NodeModel>(0);
		loader.accept(accessingNodes);
		final ArrayList<NodeModel> dependencies = new ArrayList<NodeModel>();
		dependencies.addAll(accessingNodes);
		FormulaCache.removeFromCache(dependencies);
		return dependencies;
	}
}
