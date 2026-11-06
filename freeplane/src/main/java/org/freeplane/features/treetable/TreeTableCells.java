package org.freeplane.features.treetable;

import java.util.ArrayList;
import java.util.List;

import org.freeplane.core.util.HtmlUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

public class TreeTableCells {
	private static final String CONTENT_INDENT = "\u00A0\u00A0\u00A0\u00A0";

	public static NodeModel cellAbove(final NodeModel node) {
		NodeModel cell = null;
		for (NodeModel ancestor = node; ancestor != null; ancestor = ancestor.getParentNode()) {
			if (TreeTableModel.isTreeTableRoot(ancestor))
				return cell != null && cell.getParentNode() != ancestor ? cell : null;
			if (TreeTableHeader.isHeaderRoot(ancestor))
				return null;
			if (TreeTableCellModel.isMarkedAsCell(ancestor))
				cell = ancestor;
		}
		return null;
	}

	public static boolean isCell(final NodeModel node) {
		return TreeTableCellModel.isMarkedAsCell(node) && cellAbove(node) == node;
	}

	public static boolean isInCell(final NodeModel node) {
		return cellAbove(node) != null;
	}

	public static boolean isCellContent(final NodeModel node) {
		final NodeModel cell = cellAbove(node);
		return cell != null && cell != node;
	}

	public static NodeModel newCell(final MapModel map, final String columnKey, final Object content) {
		final NodeModel cell = new NodeModel(columnKey, map);
		markAsCell(cell);
		cell.insert(new NodeModel(content, map));
		return cell;
	}

	public static void markAsCell(final NodeModel node) {
		TreeTableCellModel.markAsCell(node);
	}

	public static String contentOf(final NodeModel cell) {
		if (cell == null || !cell.hasChildren())
			return null;
		final NodeModel firstContent = cell.getChildAt(0);
		if (cell.getChildCount() == 1 && !firstContent.hasChildren())
			return firstContent.getText();
		final List<String> lines = new ArrayList<String>();
		collectContentLines(cell, "", lines);
		return String.join("\n", lines);
	}

	private static void collectContentLines(final NodeModel parent, final String indent, final List<String> lines) {
		for (final NodeModel child : parent.getChildren()) {
			lines.add(indent + HtmlUtils.htmlToPlain(child.getText()));
			collectContentLines(child, indent + CONTENT_INDENT, lines);
		}
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

	public static int leadingCellCount(final NodeModel row) {
		int count = 0;
		for (final NodeModel child : row.getChildren()) {
			if (!isCell(child))
				break;
			count++;
		}
		return count;
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
