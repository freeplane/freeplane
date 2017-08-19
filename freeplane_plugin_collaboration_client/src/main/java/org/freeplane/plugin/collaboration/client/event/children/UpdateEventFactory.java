package org.freeplane.plugin.collaboration.client.event.children;

import java.util.ArrayList;
import java.util.List;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNode;
import org.freeplane.plugin.collaboration.client.event.ImmutableChildrenUpdated;
import org.freeplane.plugin.collaboration.client.event.MapUpdated;
import org.freeplane.plugin.collaboration.client.event.children.SpecialNodeTypeUpdated.SpecialNodeType;

public class UpdateEventFactory {

	public ChildrenUpdated createChildrenUpdatedEvent(final NodeModel parent) {
		final List<NodeModel> childNodes = parent.getChildren();
		List<String> childIds = new ArrayList<>(childNodes.size());
		for (NodeModel child : childNodes) {
			childIds.add(child.createID());
		}
		return ImmutableChildrenUpdated.builder().nodeId(parent.createID()).content(childIds).build();
	}

	public MapUpdated createSpecialNodeTypeUpdatedEvent(NodeModel node) {
		final SpecialNodeType content;
		final boolean isFirstGroupNode = SummaryNode.isFirstGroupNode(node);
		final boolean isSummaryNode = SummaryNode.isSummaryNode(node);
		if(isSummaryNode && isFirstGroupNode)
			content = SpecialNodeType.SUMMARY_BEGIN_END;
		else if(isFirstGroupNode)
			content = SpecialNodeType.SUMMARY_BEGIN;
		else if(isSummaryNode)
			content = SpecialNodeType.SUMMARY_END;
		else
			throw new IllegalArgumentException();
		
		return ImmutableSpecialNodeTypeUpdated.builder().nodeId(node.createID()).content(content).build();
	}

}
