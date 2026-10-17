package org.freeplane.features.treetable.mindmapmode;

import org.freeplane.core.undo.IActor;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.treetable.TreeTableColumns;
import org.freeplane.features.treetable.TreeTableModel;
import org.freeplane.features.treetable.mindmapmode.TreeTableAppearance.NodeAppearance;

class ToggleTreeTableActor implements IActor {
	private final NodeModel node;
	private final boolean wasTreeTableRoot;
	private final TreeTableColumns previousColumns;
	private final TreeTableColumns enabledColumns;

	private NodeAppearance replacedAppearance;

	ToggleTreeTableActor(final NodeModel node) {
		this.node = node;
		this.wasTreeTableRoot = TreeTableModel.isTreeTableRoot(node);
		this.previousColumns = TreeTableModel.columnsOf(node);
		this.enabledColumns = previousColumns.isEmpty() ? TreeTableEditing.withNewColumn(previousColumns)
				: previousColumns;
	}

	@Override
	public void act() {
		if (wasTreeTableRoot)
			TreeTableModel.removeFrom(node);
		else {
			TreeTableModel.createFor(node).setColumns(enabledColumns);
			replacedAppearance = TreeTableAppearance.applyTo(node);
		}
		fireNodeChanged();
	}

	@Override
	public void undo() {
		if (wasTreeTableRoot)
			TreeTableModel.createFor(node).setColumns(previousColumns);
		else {
			TreeTableModel.removeFrom(node);
			if (replacedAppearance != null) {
				replacedAppearance.restore();
				replacedAppearance = null;
			}
		}
		fireNodeChanged();
	}

	private void fireNodeChanged() {
		Controller.getCurrentModeController().getMapController()
			.nodeChanged(node, TreeTableModel.class, null, null);
	}

	@Override
	public String getDescription() {
		return "toggleTreeTable";
	}
}
