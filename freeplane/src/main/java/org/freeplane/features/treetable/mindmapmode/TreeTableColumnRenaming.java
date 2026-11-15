package org.freeplane.features.treetable.mindmapmode;

import java.util.Collections;
import java.util.List;

import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.treetable.TreeTableHeader;

class TreeTableColumnRenaming implements INodeChangeListener {

	@Override
	public void nodeChanged(final NodeChangeEvent event) {
		final NodeModel node = event.getNode();
		if (!NodeModel.NODE_TEXT.equals(event.getProperty()) || node.getMap().isUndoActionRunning()
				|| !TreeTableHeader.isBelowHeaderRoot(node))
			return;
		for (final NodeModel columnNode : columnsAtOrBelow(node))
			TreeTableEditing.renameColumnValues(columnNode,
				TreeTableHeader.keyOf(columnNode, node, event.getOldValue()),
				TreeTableHeader.keyOf(columnNode, node, event.getNewValue()));
		TreeTableEditing.completeCells(node);
	}

	private static List<NodeModel> columnsAtOrBelow(final NodeModel node) {
		return node.hasChildren() ? TreeTableHeader.columnNodesOf(node) : Collections.singletonList(node);
	}
}
