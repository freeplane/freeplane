package org.freeplane.features.map;

public class NodeMoveEvent {
	final public NodeModel oldParent;
	final public int oldIndex;
	final public NodeModel newParent;
	final public NodeModel child;
	final public int newIndex;
	public NodeMoveEvent(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child,
			int newIndex) {
		super();
		this.oldParent = oldParent;
		this.oldIndex = oldIndex;
		this.newParent = newParent;
		this.child = child;
		this.newIndex = newIndex;
	}
	
}
