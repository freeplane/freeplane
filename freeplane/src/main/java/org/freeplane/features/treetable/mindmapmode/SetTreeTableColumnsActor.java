package org.freeplane.features.treetable.mindmapmode;

import org.freeplane.core.undo.IActor;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.treetable.TreeTableColumns;
import org.freeplane.features.treetable.TreeTableModel;

class SetTreeTableColumnsActor implements IActor {
	private final NodeModel treeTableRoot;
	private final TreeTableColumns previousColumns;
	private final TreeTableColumns newColumns;
	private final String description;

	SetTreeTableColumnsActor(final NodeModel treeTableRoot, final TreeTableColumns newColumns,
			final String description) {
		this.treeTableRoot = treeTableRoot;
		this.previousColumns = TreeTableModel.columnsOf(treeTableRoot);
		this.newColumns = newColumns;
		this.description = description;
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
		return description;
	}
}
