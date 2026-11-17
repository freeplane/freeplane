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

	public static NodeModel newCell(final MapModel map, final String columnKey, final List<CellTextTree.Node> content) {
		if (content.isEmpty())
			return newCell(map, columnKey, "");
		final NodeModel cell = new NodeModel(columnKey, map);
		markAsCell(cell);
		insertContent(cell, content, map);
		return cell;
	}

	private static void insertContent(final NodeModel parent, final List<CellTextTree.Node> content, final MapModel map) {
		for (final CellTextTree.Node item : content) {
			final NodeModel node = new NodeModel(item.getContent(), map);
			parent.insert(node);
			insertContent(node, item.getChildren(), map);
		}
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

	public static RichText richTextOf(final NodeModel cell) {
		if (cell == null || !cell.hasChildren())
			return RichText.EMPTY;
		final NodeModel firstContent = cell.getChildAt(0);
		if (cell.getChildCount() == 1 && !firstContent.hasChildren())
			return RichText.fromContent(firstContent.getText());
		final List<RichText.Run> runs = new ArrayList<RichText.Run>();
		collectContentRuns(cell, 0, runs);
		return RichText.ofRuns(runs);
	}

	private static void collectContentRuns(final NodeModel parent, final int depth, final List<RichText.Run> runs) {
		for (final NodeModel child : parent.getChildren()) {
			if (!runs.isEmpty())
				runs.add(RichText.lineBreakRun());
			if (depth > 0)
				runs.add(new RichText.Run(indentOf(depth), RichTextStyle.PLAIN));
			runs.addAll(RichText.fromContent(child.getText()).getRuns());
			collectContentRuns(child, depth + 1, runs);
		}
	}

	private static String indentOf(final int depth) {
		final StringBuilder indent = new StringBuilder();
		for (int level = 0; level < depth; level++)
			indent.append(CONTENT_INDENT);
		return indent.toString();
	}

	public static boolean hasContent(final NodeModel cell) {
		return !richTextOf(cell).isEmpty();
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

	public static NodeModel blankContentAt(final NodeModel target) {
		if (target == null)
			return null;
		final NodeModel cell = isCell(target) ? target : target.getParentNode();
		if (cell == null || !isCell(cell) || cell.getChildCount() != 1)
			return null;
		final NodeModel content = cell.getChildAt(0);
		final boolean isTargeted = target == cell || target == content;
		return isTargeted && isBlank(content) ? content : null;
	}

	private static boolean isBlank(final NodeModel content) {
		return !content.hasChildren() && TreeTableHeader.keyOfText(content.getText()).isEmpty();
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
