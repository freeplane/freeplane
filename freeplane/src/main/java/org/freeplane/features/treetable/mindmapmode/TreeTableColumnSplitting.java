package org.freeplane.features.treetable.mindmapmode;

import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;

class TreeTableColumnSplitting implements IMapChangeListener {

	@Override
	public void onNodeInserted(final NodeModel parent, final NodeModel child, final int newIndex) {
		if (parent.getMap().isUndoActionRunning())
			return;
		if (parent.getChildCount() == 1)
			TreeTableEditing.inheritColumnValues(parent);
		TreeTableEditing.completeCells(child);
	}

	@Override
	public void onNodeDeleted(final NodeDeletionEvent event) {
		if (event.parent.getMap().isUndoActionRunning())
			return;
		TreeTableEditing.returnColumnValues(event.parent, event.node);
		TreeTableEditing.completeCells(event.parent);
	}
}
