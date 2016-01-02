package org.freeplane.features.map.mindmapmode;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryLevels;
import org.freeplane.features.map.SummaryNode;

public class SummaryGroupEdgeListAdder {

	final private Collection<NodeModel> nodes;
	private List<NodeModel> nodesWithSummaryNodes;

	public SummaryGroupEdgeListAdder(Collection<NodeModel> nodes) {
		this.nodes = nodes;
	}
	
	private class ParentProcessedNodes {
		int lastSummaryNodeIndex = -1; 
		int lastAddedNodeIndex = -1;
		final SummaryLevels summaryLevels;
		final private NodeModel parentNode;
		ParentProcessedNodes(NodeModel parent){
			this.summaryLevels = new SummaryLevels(parent);
			this.parentNode = parent;
		}
		
		void addSummaryEdgeNodes(NodeModel node) {
			final int nodeIndex = node.getIndex();
			if(lastSummaryNodeIndex >= nodeIndex){
				if(summaryLevels.summaryLevels[nodeIndex] == 0)
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
						if(groupBeginNode != null ){
							nodesWithSummaryNodes.add(groupBeginNode);
							lastAddedNodeIndex++;
						}
						if(groupBeginNode != node ){
							nodesWithSummaryNodes.add(node);
							lastAddedNodeIndex++;
						}
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
						return;
					}
				}
				lastAddedNodeIndex++;
				nodesWithSummaryNodes.add(node);
			}
		}
	}

	public List<NodeModel> addSummaryEdgeNodes() {
		Map<NodeModel, ParentProcessedNodes> processedNodes = new HashMap<NodeModel, ParentProcessedNodes>();
		nodesWithSummaryNodes = new LinkedList<NodeModel>();
		for(NodeModel node : nodes){
			final NodeModel parentNode = node.getParentNode();
			if(parentNode != null) {
				ParentProcessedNodes parentProcessedNodes = processedNodes.get(parentNode);
				if(parentProcessedNodes == null){
					parentProcessedNodes = new ParentProcessedNodes(parentNode);
					processedNodes.put(parentNode, parentProcessedNodes);
				}
				parentProcessedNodes.addSummaryEdgeNodes(node);
			}
			else
				nodesWithSummaryNodes.add(node);
		}
		return nodesWithSummaryNodes;	
	}

}
