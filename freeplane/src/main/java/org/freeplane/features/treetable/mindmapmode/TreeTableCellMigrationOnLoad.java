package org.freeplane.features.treetable.mindmapmode;

import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.treetable.TreeTableCellMigration;
import org.freeplane.features.treetable.TreeTableGridCompletion;
import org.freeplane.features.treetable.TreeTableModel;

class TreeTableCellMigrationOnLoad implements IMapLifeCycleListener {

	@Override
	public void onCreate(final MapModel map) {
		if (map.getRootNode() != null && migrateTablesIn(map.getRootNode()))
			TreeTableAppearance.ensureStylesExistIn(map);
	}

	private boolean migrateTablesIn(final NodeModel node) {
		boolean containsTables = TreeTableModel.isTreeTableRoot(node);
		if (containsTables) {
			TreeTableCellMigration.migrate(node);
			TreeTableGridCompletion.completeWithoutUndo(node);
		}
		for (final NodeModel child : node.getChildren())
			containsTables |= migrateTablesIn(child);
		return containsTables;
	}
}
