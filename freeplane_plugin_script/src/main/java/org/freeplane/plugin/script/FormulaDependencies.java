package org.freeplane.plugin.script;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

public class FormulaDependencies{
	public static List<NodeModel> manageChangeAndReturnDependencies(boolean includeChanged, final NodeModel... nodes) {
		final ArrayList<NodeModel> dependencies = getAllDependencies(includeChanged, nodes);
		FormulaCache.manageChange(dependencies);
		return dependencies;
	}

	public static List<NodeModel> manageChangeAndReturnGlobalDependencies(MapModel map) {
		final ArrayList<NodeModel> dependencies = getGlobalDependencies(map);
		FormulaCache.manageChange(dependencies);
		return dependencies;
	}

	private static ArrayList<NodeModel> getAllDependencies(boolean includeChanged, final NodeModel... nodes) {
		final ArrayList<NodeModel> dependencies = new ArrayList<NodeModel>();
		for (int i = 0; i < nodes.length; i++) {
			final LinkedHashSet<NodeModel> nodeDependencies = new LinkedHashSet<NodeModel>(0);
			EvaluationDependencies.of(nodes[i].getMap()).getDependencies(nodeDependencies, nodes[i]);
			if (nodeDependencies != null)
				dependencies.addAll(nodeDependencies);
			if (includeChanged)
				dependencies.add(nodes[i]);
		}
		return dependencies;
	}


	private static ArrayList<NodeModel> getGlobalDependencies(MapModel map) {
		final ArrayList<NodeModel> dependencies = new ArrayList<NodeModel>();
		final LinkedHashSet<NodeModel> nodeDependencies = new LinkedHashSet<NodeModel>(0);
		EvaluationDependencies.of(map).getGlobalDependencies(nodeDependencies);
		if (nodeDependencies != null)
			dependencies.addAll(nodeDependencies);
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


}