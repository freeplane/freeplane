package org.freeplane.plugin.collaboration.client;

import java.util.List;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.SingleNodeStructureManipulator;

public class ChildrenUpdate{

	private SingleNodeStructureManipulator manipulator;
	private MapModel map;
	private UpdateSpecification specification;
	private NodeFactory nodeFactory;

	public ChildrenUpdate(SingleNodeStructureManipulator manipulator , MapModel map, NodeFactory nodeFactory, UpdateSpecification specification) {
		this.manipulator = manipulator;
		this.map = map;
		this.nodeFactory = nodeFactory;
		this.specification = specification;
	}

	public void apply() {
		NodeModel parent = map.getNodeForID(specification.nodeId());
		String content = specification.content();
		String[] nodeIds = content.split(",");
		int nodeIndex = 0;
		for(String nodeId : nodeIds) {
			NodeModel child = nodeFactory.createNode(map, nodeId);
			manipulator.insertNode(child, parent, nodeIndex++, false);
		}
	}
}
