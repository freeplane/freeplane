package org.freeplane.features.treetable;

import java.util.ArrayList;
import java.util.List;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleModel;
import org.freeplane.features.styles.StyleFactory;

public class TreeTableCells {
	public static final String STYLE_NAME = "TreeCell";

	public static IStyle style() {
		return StyleFactory.create(STYLE_NAME);
	}

	public static NodeModel cellAbove(final NodeModel node) {
		NodeModel cell = null;
		for (NodeModel ancestor = node; ancestor != null; ancestor = ancestor.getParentNode()) {
			if (TreeTableModel.isTreeTableRoot(ancestor))
				return cell != null && cell.getParentNode() != ancestor ? cell : null;
			if (TreeTableHeader.isHeaderRoot(ancestor))
				return null;
			if (hasCellStyle(ancestor))
				cell = ancestor;
		}
		return null;
	}

	private static boolean hasCellStyle(final NodeModel node) {
		return style().equals(LogicalStyleModel.getStyle(node));
	}

	public static boolean isCell(final NodeModel node) {
		return cellAbove(node) == node;
	}

	public static boolean isInCell(final NodeModel node) {
		return cellAbove(node) != null;
	}

	public static boolean isCellContent(final NodeModel node) {
		final NodeModel cell = cellAbove(node);
		return cell != null && cell != node;
	}

	public static String keyOf(final NodeModel cell) {
		return TreeTableHeader.keyOf(cell);
	}

	public static List<NodeModel> cellsOf(final NodeModel row) {
		final List<NodeModel> cells = new ArrayList<NodeModel>();
		for (final NodeModel child : row.getChildren()) {
			if (isCell(child))
				cells.add(child);
		}
		return cells;
	}

	public static NodeModel cellOf(final NodeModel row, final String columnKey) {
		for (final NodeModel cell : cellsOf(row)) {
			if (keyOf(cell).equals(columnKey))
				return cell;
		}
		return null;
	}

	public static List<NodeModel> subRowsOf(final NodeModel row) {
		final List<NodeModel> subRows = new ArrayList<NodeModel>();
		for (final NodeModel child : row.getChildren()) {
			if (!isCell(child))
				subRows.add(child);
		}
		return subRows;
	}

	private TreeTableCells() {
	}
}
