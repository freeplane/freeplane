package org.freeplane.features.map;


abstract public class AMapChangeListenerAdapter implements IMapChangeListener, INodeChangeListener, IMapLifeCycleListener {
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

	public void onCreate(MapModel map) {
    }

	public void onRemove(MapModel map) {
    }
	
	
}
