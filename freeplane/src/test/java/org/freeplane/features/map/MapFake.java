package org.freeplane.features.map;

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
		final NodeModel summaryNode = createSummaryNode();
		root.insert(summaryNode);
		return summaryNode;
	}


	public NodeModel createSummaryNode() {
		final NodeModel summaryNode = createNode();
		summaryNode.addExtension(SummaryNodeFlag.SUMMARY);
		return summaryNode;
	}


	public NodeModel createNode(String string) {
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
		final NodeModel firstEdge = createGroupBeginNode();
		root.insert(firstEdge);
		return firstEdge;
	}


	public NodeModel createGroupBeginNode() {
		final NodeModel firstEdge = createNode();
		firstEdge.addExtension(FirstGroupNodeFlag.FIRST_GROUP);
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