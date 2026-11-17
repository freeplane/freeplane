package org.freeplane.features.treetable;

import java.util.ArrayList;
import java.util.List;

import org.freeplane.features.map.NodeModel;

public class TreeTableGridCompletion {
	public static class Gap {
		private final NodeModel row;
		private final String columnKey;
		private final NodeModel emptyCell;

		private Gap(final NodeModel row, final String columnKey, final NodeModel emptyCell) {
			this.row = row;
			this.columnKey = columnKey;
			this.emptyCell = emptyCell;
		}

		public NodeModel getRow() {
			return row;
		}

		public String getColumnKey() {
			return columnKey;
		}

		public NodeModel getEmptyCell() {
			return emptyCell;
		}
	}

	public static List<Gap> gapsOf(final NodeModel treeTableRoot) {
		final List<Gap> gaps = new ArrayList<Gap>();
		final TreeTableColumns columns = TreeTableModel.columnsOf(treeTableRoot);
		for (final NodeModel row : TreeTableNodes.rowsOf(treeTableRoot)) {
			for (int index = 0; index < columns.size(); index++) {
				final String key = columns.get(index).getAttributeKey();
				final NodeModel cell = TreeTableCells.cellOf(row, key);
				if (cell == null || !cell.hasChildren())
					gaps.add(new Gap(row, key, cell));
			}
		}
		return gaps;
	}

	public static List<NodeModel> emptyCellsOutsideColumnsOf(final NodeModel treeTableRoot) {
		final List<NodeModel> cells = new ArrayList<NodeModel>();
		final TreeTableColumns columns = TreeTableModel.columnsOf(treeTableRoot);
		for (final NodeModel row : TreeTableNodes.rowsOf(treeTableRoot)) {
			for (final NodeModel cell : TreeTableCells.cellsOf(row)) {
				if (!columns.containsAttributeKey(TreeTableCells.keyOf(cell)) && isEmpty(cell))
					cells.add(cell);
			}
		}
		return cells;
	}

	private static boolean isEmpty(final NodeModel cell) {
		for (final NodeModel content : cell.getChildren()) {
			if (content.hasChildren() || !TreeTableHeader.keyOf(content).isEmpty())
				return false;
		}
		return true;
	}

	public static boolean completeWithoutUndo(final NodeModel treeTableRoot) {
		final List<Gap> gaps = gapsOf(treeTableRoot);
		for (final Gap gap : gaps) {
			if (gap.emptyCell != null)
				gap.emptyCell.insert(new NodeModel("", treeTableRoot.getMap()));
			else
				gap.row.insert(TreeTableCells.newCell(treeTableRoot.getMap(), gap.columnKey, ""),
					TreeTableCells.leadingCellCount(gap.row));
		}
		return !gaps.isEmpty();
	}

	private TreeTableGridCompletion() {
	}
}
