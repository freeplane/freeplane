package org.freeplane.features.treetable.mindmapmode;

import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.NodeModel;

class TreeTableColumnSplitting implements IMapChangeListener {

	@Override
	public void onNodeInserted(final NodeModel parent, final NodeModel child, final int newIndex) {
		if (parent.getChildCount() == 1 && !parent.getMap().isUndoActionRunning())
			TreeTableEditing.inheritColumnValues(parent);
	}
}
