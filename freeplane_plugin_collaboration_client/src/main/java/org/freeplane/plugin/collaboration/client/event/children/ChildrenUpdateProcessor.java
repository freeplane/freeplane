package org.freeplane.plugin.collaboration.client.event.children;

import java.util.List;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.SingleNodeStructureManipulator;
import org.freeplane.plugin.collaboration.client.event.UpdateProcessor;

public class ChildrenUpdateProcessor implements UpdateProcessor<ChildrenUpdated> {

	private SingleNodeStructureManipulator manipulator;
	private NodeFactory nodeFactory;

	public ChildrenUpdateProcessor(SingleNodeStructureManipulator manipulator , NodeFactory nodeFactory) {
		this.manipulator = manipulator;
		this.nodeFactory = nodeFactory;
	}

	@Override
	public void onUpdate(MapModel map, ChildrenUpdated event) {
		NodeModel parent = map.getNodeForID(event.nodeId());
		List<String> nodeIds = event.content();
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
