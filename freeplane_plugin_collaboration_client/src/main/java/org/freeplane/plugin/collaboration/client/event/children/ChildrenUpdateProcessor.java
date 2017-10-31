package org.freeplane.plugin.collaboration.client.event.children;

import java.util.List;
import java.util.Optional;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.SingleNodeStructureManipulator;
import org.freeplane.plugin.collaboration.client.event.UpdateProcessor;
import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdated.Side;

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
		Optional<Side> side = Optional.empty();
		for(String nodeId : nodeIds) {
			if(! side.isPresent()) {
				side = Side.of(nodeId);
				if(side.isPresent())
					continue;
			}
			NodeModel existingNode = map.getNodeForID(nodeId);
			boolean isLeft = side.map(s -> s == Side.LEFT).orElse(parent.isLeft());
			if(existingNode != null) {
				manipulator.moveNode(existingNode, parent, nodeIndex, isLeft, isLeft != existingNode.isLeft());
			}
			else {
				NodeModel child = nodeFactory.createNode(map);
				child.setID(nodeId);
				manipulator.insertNode(child, parent, nodeIndex, isLeft);
			}
			side = Optional.empty();
			nodeIndex++;
		}
		for(int i = parent.getChildCount() - 1; i >= nodeIndex ; i--) {
			manipulator.deleteNode(parent, nodeIndex);
		}
	}

	@Override
	public Class<ChildrenUpdated> eventClass() {
		return ChildrenUpdated.class;
	}
}
