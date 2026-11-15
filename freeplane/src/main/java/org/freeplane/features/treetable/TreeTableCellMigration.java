package org.freeplane.features.treetable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.NodeModel;

public class TreeTableCellMigration {

	public static boolean migrate(final NodeModel treeTableRoot) {
		final List<NodeModel> columnNodes = TreeTableHeader.distinctColumnNodesOf(treeTableRoot);
		boolean migrated = false;
		for (final NodeModel row : TreeTableNodes.rowsOf(treeTableRoot)) {
			migrated |= migrateLegacyCellKeys(row, columnNodes);
			migrated |= migrateAttributes(row, columnNodes);
		}
		return migrated;
	}

	private static boolean migrateLegacyCellKeys(final NodeModel row, final List<NodeModel> columnNodes) {
		final Set<String> columnKeys = new HashSet<String>();
		for (final NodeModel columnNode : columnNodes)
			columnKeys.add(TreeTableHeader.keyOf(columnNode));
		boolean migrated = false;
		for (final NodeModel cell : TreeTableCells.cellsOf(row)) {
			final String key = TreeTableCells.keyOf(cell);
			if (columnKeys.contains(key))
				continue;
			for (final NodeModel columnNode : columnNodes) {
				final String columnKey = TreeTableHeader.keyOf(columnNode);
				if (TreeTableHeader.leafNameOf(columnNode).equals(key) && TreeTableCells.cellOf(row, columnKey) == null) {
					cell.setUserObject(columnKey);
					migrated = true;
					break;
				}
			}
		}
		return migrated;
	}

	private static boolean migrateAttributes(final NodeModel row, final List<NodeModel> columnNodes) {
		final NodeAttributeTableModel attributes = row.getExtension(NodeAttributeTableModel.class);
		if (attributes == null)
			return false;
		final List<Attribute> rowAttributes = attributes.getAttributes();
		int cellIndex = TreeTableCells.cellsOf(row).size();
		boolean migrated = false;
		for (final NodeModel columnNode : columnNodes) {
			final String key = TreeTableHeader.keyOf(columnNode);
			final int attributeIndex = attributes.getAttributeIndex(TreeTableHeader.leafNameOf(columnNode));
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
