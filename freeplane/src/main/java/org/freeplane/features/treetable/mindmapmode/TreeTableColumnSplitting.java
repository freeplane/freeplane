package org.freeplane.features.treetable.mindmapmode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.features.treetable.TreeTableHeader;

class TreeTableColumnSplitting implements IMapChangeListener {
	private List<NodeModel> movedColumns = Collections.emptyList();
	private List<String> movedColumnKeys = Collections.emptyList();

	@Override
	public void onPreNodeMoved(final NodeMoveEvent event) {
		movedColumns = Collections.emptyList();
		movedColumnKeys = Collections.emptyList();
		if (event.oldParent.getMap().isUndoActionRunning() || !TreeTableHeader.isBelowHeaderRoot(event.child))
			return;
		movedColumns = event.child.hasChildren() ? TreeTableHeader.columnNodesOf(event.child)
				: Collections.singletonList(event.child);
		movedColumnKeys = new ArrayList<String>();
		for (final NodeModel column : movedColumns)
			movedColumnKeys.add(TreeTableHeader.keyOf(column));
	}

	@Override
	public void onNodeMoved(final NodeMoveEvent event) {
		final List<NodeModel> columns = movedColumns;
		final List<String> oldKeys = movedColumnKeys;
		movedColumns = Collections.emptyList();
		movedColumnKeys = Collections.emptyList();
		if (event.child.getMap().isUndoActionRunning())
			return;
		for (int index = 0; index < columns.size(); index++) {
			final NodeModel column = columns.get(index);
			TreeTableEditing.renameColumnValues(column, oldKeys.get(index), TreeTableHeader.keyOf(column));
		}
		if (!columns.isEmpty())
			TreeTableEditing.completeCells(event.child);
		TreeTableEditing.completeCells(event.oldParent);
	}

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
