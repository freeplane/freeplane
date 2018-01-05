package org.freeplane.plugin.collaboration.client.event.children_deprecated;

import java.util.List;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.SingleNodeStructureManipulator;
import org.freeplane.plugin.collaboration.client.event.UpdateProcessor;
import org.freeplane.plugin.collaboration.client.event.children_deprecated.ChildrenUpdated.Child;
import org.freeplane.plugin.collaboration.client.event.children_deprecated.ChildrenUpdated.Side;

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
		List<Child> children = event.content();
		int nodeIndex = 0;
		for(Child child : children) {
			NodeModel existingNode = map.getNodeForID(child.id());
			boolean isLeft = child.side().map(Side::isLeft).orElse(parent.isLeft());
			if(existingNode != null) {
				manipulator.moveNode(existingNode, parent, nodeIndex, isLeft, isLeft != existingNode.isLeft());
			}
			else {
				NodeModel node = nodeFactory.createNode(map);
				node.setID(child.id());
				manipulator.insertNode(node, parent, nodeIndex, isLeft);
			}
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
