package org.freeplane.features.treetable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.freeplane.features.map.NodeModel;

public class TreeTableNavigation {
	public enum Direction {
		LEFT, RIGHT, UP, DOWN
	}

	private final Predicate<NodeModel> isShown;

	public TreeTableNavigation(final Predicate<NodeModel> isShown) {
		this.isShown = isShown;
	}

	public NodeModel target(final NodeModel selected, final Direction direction) {
		if (TreeTableModel.isTreeTableRoot(selected))
			return targetFromTreeTableRoot(selected, direction);
		if (TreeTableHeader.isColumnNode(selected))
			return targetFromColumn(selected, direction);
		if (TreeTableHeader.isInHeader(selected))
			return targetFromHeaderGroup(selected, direction);
		if (TreeTableNodes.isRow(selected))
			return targetFromRow(selected, direction);
		final NodeModel cell = TreeTableCells.cellAbove(selected);
		if (cell == null || cell == selected)
			return null;
		return targetFromCellContent(selected, cell, direction);
	}

	private NodeModel targetFromTreeTableRoot(final NodeModel treeTableRoot, final Direction direction) {
		switch (direction) {
		case RIGHT:
			final NodeModel header = TreeTableHeader.headerOf(treeTableRoot);
			return header != null && isShown.test(header) ? header : null;
		case DOWN:
			final List<NodeModel> rows = shownRowsOf(treeTableRoot);
			return rows.isEmpty() ? null : rows.get(0);
		default:
			return null;
		}
	}

	private NodeModel targetFromHeaderGroup(final NodeModel group, final Direction direction) {
		final boolean isHeaderRoot = TreeTableHeader.isHeaderRoot(group);
		switch (direction) {
		case DOWN:
			final List<NodeModel> children = shownChildrenOf(group);
			return children.isEmpty() ? null : children.get(0);
		case UP:
			return isHeaderRoot ? null : group.getParentNode();
		case LEFT:
			return isHeaderRoot ? group.getParentNode() : shownSibling(group, -1);
		case RIGHT:
			return isHeaderRoot ? null : shownSibling(group, 1);
		default:
			return null;
		}
	}

	private NodeModel shownSibling(final NodeModel node, final int step) {
		final List<NodeModel> siblings = shownChildrenOf(node.getParentNode());
		final int index = siblings.indexOf(node) + step;
		return index >= 0 && index < siblings.size() ? siblings.get(index) : null;
	}

	private NodeModel targetFromColumn(final NodeModel column, final Direction direction) {
		final NodeModel treeTableRoot = TreeTableNodes.treeTableRootOf(column);
		final List<NodeModel> columns = shownColumnsOf(treeTableRoot);
		final int index = columns.indexOf(column);
		if (index < 0)
			return null;
		switch (direction) {
		case DOWN:
			return firstContentBelow(shownRowsOf(treeTableRoot), -1, TreeTableHeader.keyOf(column));
		case LEFT:
			return index > 0 ? columns.get(index - 1) : null;
		case RIGHT:
			return index < columns.size() - 1 ? columns.get(index + 1) : null;
		case UP:
			return column.getParentNode();
		default:
			return null;
		}
	}

	private NodeModel targetFromRow(final NodeModel row, final Direction direction) {
		switch (direction) {
		case RIGHT:
			return firstContentOfCellAfter(row, -1);
		case UP:
			final NodeModel previousRow = adjacentRow(row, -1);
			return previousRow != null ? previousRow : TreeTableNodes.treeTableRootOf(row);
		case DOWN:
			return adjacentRow(row, 1);
		default:
			return null;
		}
	}

	private NodeModel targetFromCellContent(final NodeModel content, final NodeModel cell, final Direction direction) {
		final NodeModel row = cell.getParentNode();
		final String key = TreeTableCells.keyOf(cell);
		final int columnIndex = columnKeysOf(row).indexOf(key);
		switch (direction) {
		case RIGHT:
			final NodeModel next = firstContentOfCellAfter(row, columnIndex);
			return next != null ? next : content;
		case LEFT:
			if (content.getParentNode() != cell)
				return null;
			final NodeModel previous = firstContentOfCellBefore(row, columnIndex);
			return previous != null ? previous : row;
		case UP:
			if (!isFirstShownIn(content, cell))
				return null;
			final NodeModel above = lastContentAbove(row, key);
			return above != null ? above : columnOrSelf(row, key, content);
		case DOWN:
			if (!isLastShownIn(content, cell))
				return null;
			final List<NodeModel> rows = shownRowsOf(TreeTableNodes.treeTableRootOf(row));
			final NodeModel below = firstContentBelow(rows, rows.indexOf(row), key);
			return below != null ? below : content;
		default:
			return null;
		}
	}

	private NodeModel columnOrSelf(final NodeModel row, final String key, final NodeModel content) {
		for (final NodeModel column : shownColumnsOf(TreeTableNodes.treeTableRootOf(row))) {
			if (TreeTableHeader.keyOf(column).equals(key))
				return column;
		}
		return content;
	}

	private NodeModel firstContentBelow(final List<NodeModel> rows, final int rowIndex, final String key) {
		for (int index = rowIndex + 1; index < rows.size(); index++) {
			final List<NodeModel> contents = shownChildrenOf(TreeTableCells.cellOf(rows.get(index), key));
			if (!contents.isEmpty())
				return contents.get(0);
		}
		return null;
	}

	private NodeModel lastContentAbove(final NodeModel row, final String key) {
		final List<NodeModel> rows = shownRowsOf(TreeTableNodes.treeTableRootOf(row));
		for (int index = rows.indexOf(row) - 1; index >= 0; index--) {
			final List<NodeModel> contents = shownChildrenOf(TreeTableCells.cellOf(rows.get(index), key));
			if (!contents.isEmpty())
				return lastShownDescendantOf(contents.get(contents.size() - 1));
		}
		return null;
	}

	private NodeModel lastShownDescendantOf(final NodeModel node) {
		final List<NodeModel> children = shownChildrenOf(node);
		return children.isEmpty() ? node : lastShownDescendantOf(children.get(children.size() - 1));
	}

	private NodeModel adjacentRow(final NodeModel row, final int step) {
		final List<NodeModel> rows = shownRowsOf(TreeTableNodes.treeTableRootOf(row));
		final int index = rows.indexOf(row) + step;
		return index >= 0 && index < rows.size() && rows.contains(row) ? rows.get(index) : null;
	}

	private List<NodeModel> shownRowsOf(final NodeModel treeTableRoot) {
		final List<NodeModel> rows = new ArrayList<NodeModel>();
		for (final NodeModel row : TreeTableNodes.rowsOf(treeTableRoot)) {
			if (isShown.test(row))
				rows.add(row);
		}
		return rows;
	}

	private List<NodeModel> shownColumnsOf(final NodeModel treeTableRoot) {
		final List<NodeModel> columns = new ArrayList<NodeModel>();
		for (final NodeModel column : TreeTableHeader.distinctColumnNodesOf(treeTableRoot)) {
			if (isShown.test(column))
				columns.add(column);
		}
		return columns;
	}

	private NodeModel firstContentOfCellAfter(final NodeModel row, final int columnIndex) {
		final List<String> keys = columnKeysOf(row);
		for (int index = columnIndex + 1; index < keys.size(); index++) {
			final List<NodeModel> contents = shownChildrenOf(TreeTableCells.cellOf(row, keys.get(index)));
			if (!contents.isEmpty())
				return contents.get(0);
		}
		return null;
	}

	private NodeModel firstContentOfCellBefore(final NodeModel row, final int columnIndex) {
		final List<String> keys = columnKeysOf(row);
		for (int index = columnIndex - 1; index >= 0; index--) {
			final List<NodeModel> contents = shownChildrenOf(TreeTableCells.cellOf(row, keys.get(index)));
			if (!contents.isEmpty())
				return contents.get(0);
		}
		return null;
	}

	private boolean isFirstShownIn(final NodeModel content, final NodeModel cell) {
		final List<NodeModel> contents = shownChildrenOf(cell);
		return !contents.isEmpty() && contents.get(0) == content;
	}

	private boolean isLastShownIn(final NodeModel content, final NodeModel cell) {
		if (!shownChildrenOf(content).isEmpty())
			return false;
		for (NodeModel node = content; node != cell; node = node.getParentNode()) {
			final List<NodeModel> siblings = shownChildrenOf(node.getParentNode());
			if (siblings.get(siblings.size() - 1) != node)
				return false;
		}
		return true;
	}

	private List<NodeModel> shownChildrenOf(final NodeModel node) {
		final List<NodeModel> children = new ArrayList<NodeModel>();
		if (node == null)
			return children;
		for (final NodeModel child : node.getChildren()) {
			if (isShown.test(child))
				children.add(child);
		}
		return children;
	}

	private static List<String> columnKeysOf(final NodeModel row) {
		final TreeTableColumns columns = TreeTableModel.columnsOf(TreeTableNodes.treeTableRootOf(row));
		final List<String> keys = new ArrayList<String>();
		for (int index = 0; index < columns.size(); index++)
			keys.add(columns.get(index).getAttributeKey());
		return keys;
	}
}
