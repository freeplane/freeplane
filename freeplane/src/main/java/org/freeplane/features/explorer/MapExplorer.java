package org.freeplane.features.explorer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.freeplane.features.map.NodeModel;

public class MapExplorer {

	private final NodeModel start;
	private final List<Command> path;

	MapExplorer(NodeModel start, String path, AccessedNodes accessedNodes) {
		this(start,
			new ExploringStepBuilder(path, accessedNodes).buildSteps());
	}



	private MapExplorer(NodeModel start, List<Command> path) {
		this.start = start;
		this.path = path;
	}

	public NodeModel getNode() {
		final List<? extends NodeModel> nodes = getNodes();
		final int nodeCount = nodes.size();
		if(nodeCount == 1)
			return nodes.get(0);
		else
			throw new NodeNotFoundException("One and only one node matching giving string expected, " + nodeCount + " nodes found");

	}

	public List<? extends NodeModel> getNodes() {
		List<NodeModel> nodes = Arrays.asList(start);
		for(Command command : path) {
			if(nodes.isEmpty())
				return nodes;
			List<NodeModel> nextNodes = new ArrayList<>();
			for(NodeModel from:nodes) {
				final Collection<? extends NodeModel> elementNodes = command.getNodes(from);
				nextNodes.addAll(elementNodes);
			}
			nodes = nextNodes;
		}
		return nodes;
	}
}
