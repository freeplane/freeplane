package org.freeplane.plugin.collaboration.client;

import java.util.List;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.SingleNodeStructureManipulator;

public class ChildrenUpdate{

	private SingleNodeStructureManipulator manipulator;
	private MapModel map;
	private ChildrenUpdateSpecification specification;
	private NodeFactory nodeFactory;

	public ChildrenUpdate(SingleNodeStructureManipulator manipulator , MapModel map, NodeFactory nodeFactory, ChildrenUpdateSpecification specification) {
		this.manipulator = manipulator;
		this.map = map;
		this.nodeFactory = nodeFactory;
		this.specification = specification;
	}

	public void apply() {
		NodeModel parent = map.getNodeForID(specification.nodeId());
		List<String> nodeIds = specification.content();
		int nodeIndex = 0;
		for(String nodeId : nodeIds) {
			NodeModel existingNode = map.getNodeForID(nodeId);
			if(existingNode != null)
				manipulator.moveNode(existingNode, parent, nodeIndex, false, false);
			else {
				NodeModel child = nodeFactory.createNode(map, nodeId);
				manipulator.insertNode(child, parent, nodeIndex, false);
			}
			nodeIndex++;
		}
		for(int i = parent.getChildCount() - 1; i >= nodeIndex ; i--) {
			manipulator.deleteNode(parent, nodeIndex);
		}
	}
}
