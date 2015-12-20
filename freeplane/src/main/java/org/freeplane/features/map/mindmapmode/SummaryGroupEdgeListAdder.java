package org.freeplane.features.map.mindmapmode;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryLevels;
import org.freeplane.features.map.SummaryNode;

public class SummaryGroupEdgeListAdder {

	final private Collection<NodeModel> nodes;

	public SummaryGroupEdgeListAdder(Collection<NodeModel> nodes) {
		this.nodes = nodes;
	}

	public List<NodeModel> addSummaryEdgeNodes() {
		List<NodeModel> nodesWithSummaryNodes = new LinkedList<>();
		int lastSummaryNodeIndex = -1;
		int lastAddedNodeIndex = -1;
		for(NodeModel node : nodes){
			final NodeModel parentNode = node.getParentNode();
			if(parentNode != null) {
				final SummaryLevels summaryLevels = new SummaryLevels(parentNode);
				final int nodeIndex = node.getIndex();
				if(lastSummaryNodeIndex > nodeIndex){
					nodesWithSummaryNodes.add(++lastAddedNodeIndex, node);
				}
				else {
					final int groupBeginNodeIndex = summaryLevels.findGroupBeginNodeIndex(nodeIndex);
					final NodeModel groupBeginNode = parentNode.getChildAt(groupBeginNodeIndex);
					lastSummaryNodeIndex = summaryLevels.findSummaryNodeIndex(nodeIndex);
					if(lastSummaryNodeIndex != SummaryLevels.NODE_NOT_FOUND ){
						final NodeModel summaryNode = parentNode.getChildAt(lastSummaryNodeIndex);
						final Collection<NodeModel> summarizedNodes = summaryLevels.summarizedNodes(summaryNode);
						if(nodes.containsAll(summarizedNodes)) {
							if(groupBeginNode != null )
								nodesWithSummaryNodes.add(groupBeginNode);
							lastAddedNodeIndex = nodesWithSummaryNodes.size();
							nodesWithSummaryNodes.add(node);
							nodesWithSummaryNodes.add(summaryNode);
							while(parentNode.getChildCount() > lastSummaryNodeIndex + 1){
								final NodeModel nextNode = parentNode.getChildAt(lastSummaryNodeIndex + 1);
								if (SummaryNode.isSummaryNode(nextNode)){
									lastSummaryNodeIndex++;
									final Collection<NodeModel> summarizedSummaries = summaryLevels.summarizedNodes(nextNode);
									if(nodesWithSummaryNodes.containsAll(summarizedSummaries)){
										nodesWithSummaryNodes.add(nextNode);
										continue;
									}
								}
								break;
							}
							continue;
						}
					}
					lastAddedNodeIndex = nodesWithSummaryNodes.size();
					nodesWithSummaryNodes.add(node);
				}
			}
		}
		return nodesWithSummaryNodes;	
	}

}
