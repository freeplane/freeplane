package org.freeplane.features.map;

public class NodeDeletionEvent {
	final public NodeModel parent;
	final public NodeModel node;
	final public int index;
	public NodeDeletionEvent(NodeModel parent, NodeModel node, int index) {
		super();
		this.parent = parent;
		this.node = node;
		this.index = index;
	}
	
}
