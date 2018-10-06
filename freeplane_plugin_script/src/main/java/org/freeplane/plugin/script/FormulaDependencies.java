package org.freeplane.plugin.script;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

public class FormulaDependencies{
	public static List<NodeModel> manageChangeAndReturnDependencies(boolean includeChanged, final NodeModel... changedNodes) {
		final ArrayList<NodeModel> dependencies = getAllChangedDependencies(includeChanged, changedNodes);
		FormulaCache.removeFromCache(dependencies);
		return dependencies;
	}

	public static List<NodeModel> manageChangeAndReturnGlobalDependencies(MapModel map) {
		final ArrayList<NodeModel> dependencies = getGlobalDependencies(map);
		FormulaCache.removeFromCache(dependencies);
		return dependencies;
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


	private static ArrayList<NodeModel> getGlobalDependencies(MapModel map) {
		final ArrayList<NodeModel> dependencies = new ArrayList<NodeModel>();
		final LinkedHashSet<NodeModel> accessingNodes = new LinkedHashSet<NodeModel>(0);
		EvaluationDependencies.of(map).getGlobalDependencies(accessingNodes);
		if (accessingNodes != null)
			dependencies.addAll(accessingNodes);
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
}
