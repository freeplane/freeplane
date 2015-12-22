package org.freeplane.features.map;

public class NodeMoveEvent {
	final public NodeModel oldParent;
	final public int oldIndex;
	final public boolean oldSideLeft;
	final public NodeModel newParent;
	final public NodeModel child;
	final public int newIndex;
	final public boolean newSideLeft;
	public NodeMoveEvent(NodeModel oldParent, int oldIndex, boolean oldSideLeft, NodeModel newParent, NodeModel child,
			int newIndex, boolean newSideLeft) {
		super();
		this.oldParent = oldParent;
		this.oldIndex = oldIndex;
		this.oldSideLeft = oldSideLeft;
		this.newParent = newParent;
		this.child = child;
		this.newIndex = newIndex;
		this.newSideLeft = newSideLeft;
	}
	
}
