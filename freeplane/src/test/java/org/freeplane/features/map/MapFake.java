package org.freeplane.features.map;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.FirstGroupNode.FirstGroupNodeFlag;
import org.freeplane.features.map.SummaryNode.SummaryNodeFlag;
import org.mockito.Mockito;

public class MapFake {
	final private NodeModel root;
	final private MapModel map;


	public MapFake() {
		super();
		map = Mockito.mock(MapModel.class);
		this.root = createNode("root");
		Mockito.when(map.getRootNode()).thenReturn(root);
	}


	public NodeModel addSummaryNode() {
		final NodeModel summaryNode = createNode();
		summaryNode.addExtension(SummaryNodeFlag.SUMMARY);
		root.insert(summaryNode);
		return summaryNode;
	}


	private NodeModel createNode(String string) {
		final NodeModel node = createNode();
		node.setText(string);
		return node;
	}

	private NodeModel createNode() {
		return new NodeModel(map);
	}
	public NodeModel addNode(String text) {
		final NodeModel summarized = createNode(text);
		root.insert(summarized);
		return summarized;
	}

	public NodeModel addGroupBeginNode() {
		final NodeModel firstEdge = createNode();
		firstEdge.addExtension(FirstGroupNodeFlag.FIRST_GROUP);
		root.insert(firstEdge);
		return firstEdge;
	}


	public NodeModel getRoot() {
		return root;
	}


	public NodeModel addSummaryGroupBeginNode() {
		final NodeModel summaryNode = addSummaryNode();
		summaryNode.addExtension(FirstGroupNodeFlag.FIRST_GROUP);
		return summaryNode;
		
	}

}