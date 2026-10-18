package org.freeplane.features.treetable.mindmapmode;

import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.treetable.TreeTableHeader;

class TreeTableColumnRenaming implements INodeChangeListener {

	@Override
	public void nodeChanged(final NodeChangeEvent event) {
		final NodeModel node = event.getNode();
		if (!NodeModel.NODE_TEXT.equals(event.getProperty()) || node.getMap().isUndoActionRunning()
				|| !TreeTableHeader.isColumnNode(node))
			return;
		TreeTableEditing.renameColumnValues(node, TreeTableHeader.keyOfText(event.getOldValue()),
			TreeTableHeader.keyOfText(event.getNewValue()));
	}
}
