package org.freeplane.features.map;


abstract public class AMapChangeListenerAdapter implements IMapChangeListener, INodeChangeListener, IMapLifeCycleListener {
	public void mapChanged(MapChangeEvent event) {
	}

	public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
	}

	public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
	}

	public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
	}

	public void onPreNodeDelete(NodeDeletionEvent nodeDeletionEvent) {
	}

	public void onPreNodeMoved(NodeMoveEvent nodeMoveEvent) {
	}

	public void nodeChanged(NodeChangeEvent event) {
	}

	public void onCreate(MapModel map) {
    }

	public void onRemove(MapModel map) {
    }
	
	
}
