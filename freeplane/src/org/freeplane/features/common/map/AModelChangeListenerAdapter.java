package org.freeplane.features.common.map;

abstract public class AModelChangeListenerAdapter implements IMapChangeListener, INodeChangeListener {
	public void mapChanged(MapChangeEvent event) {
	}

	public void onNodeDeleted(NodeModel parent, NodeModel child, int index) {
	}

	public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
	}

	public void onNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
	}

	public void onPreNodeDelete(NodeModel oldParent, NodeModel selectedNode, int index) {
	}

	public void onPreNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
	}

	public void nodeChanged(NodeChangeEvent event) {
	}
}
