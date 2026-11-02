package org.freeplane.features.treetable;

import java.util.List;

import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.NodeModel;

public class TreeTableCellMigration {

	public static boolean migrate(final NodeModel treeTableRoot) {
		final TreeTableColumns columns = TreeTableModel.columnsOf(treeTableRoot);
		boolean migrated = false;
		for (final NodeModel row : TreeTableNodes.rowsOf(treeTableRoot))
			migrated |= migrateRow(row, columns);
		return migrated;
	}

	private static boolean migrateRow(final NodeModel row, final TreeTableColumns columns) {
		final NodeAttributeTableModel attributes = row.getExtension(NodeAttributeTableModel.class);
		if (attributes == null)
			return false;
		final List<Attribute> rowAttributes = attributes.getAttributes();
		int cellIndex = TreeTableCells.cellsOf(row).size();
		boolean migrated = false;
		for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
			final String key = columns.get(columnIndex).getAttributeKey();
			final int attributeIndex = attributes.getAttributeIndex(key);
			if (attributeIndex < 0 || TreeTableCells.cellOf(row, key) != null)
				continue;
			final Object value = rowAttributes.remove(attributeIndex).getValue();
			row.insert(TreeTableCells.newCell(row.getMap(), key, value), cellIndex++);
			migrated = true;
		}
		return migrated;
	}

	private TreeTableCellMigration() {
	}
}
