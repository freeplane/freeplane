package org.freeplane.features.treetable.mindmapmode;

import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.treetable.TreeTableColumn;
import org.freeplane.features.treetable.TreeTableColumns;
import org.freeplane.features.treetable.TreeTableModel;

class AddTreeTableColumnActor implements IActor {
	private final NodeModel treeTableRoot;
	private final TreeTableColumns previousColumns;
	private final TreeTableColumns newColumns;

	AddTreeTableColumnActor(final NodeModel treeTableRoot) {
		this.treeTableRoot = treeTableRoot;
		this.previousColumns = TreeTableModel.columnsOf(treeTableRoot);
		this.newColumns = previousColumns.appended(new TreeTableColumn(unusedColumnName(previousColumns)));
	}

	private static String unusedColumnName(final TreeTableColumns columns) {
		final String prefix = TextUtils.getText("treetable.new_column");
		for (int index = columns.size() + 1;; index++) {
			final String name = prefix + " " + index;
			if (!columns.containsAttributeKey(name))
				return name;
		}
	}

	@Override
	public void act() {
		setColumns(newColumns);
	}

	@Override
	public void undo() {
		setColumns(previousColumns);
	}

	private void setColumns(final TreeTableColumns columns) {
		TreeTableModel.createFor(treeTableRoot).setColumns(columns);
		Controller.getCurrentModeController().getMapController()
			.nodeChanged(treeTableRoot, TreeTableModel.class, null, null);
	}

	@Override
	public String getDescription() {
		return "addTreeTableColumn";
	}
}
