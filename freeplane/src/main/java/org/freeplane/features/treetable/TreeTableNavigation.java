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
		if (TreeTableNodes.isRow(selected))
			return targetFromRow(selected, direction);
		final NodeModel cell = TreeTableCells.cellAbove(selected);
		if (cell == null || cell == selected)
			return null;
		return targetFromCellContent(selected, cell, direction);
	}

	private NodeModel targetFromRow(final NodeModel row, final Direction direction) {
		switch (direction) {
		case RIGHT:
			return firstContentOfCellAfter(row, -1);
		case UP:
			return adjacentRow(row, -1);
		case DOWN:
			return adjacentRow(row, 1);
		default:
			return null;
		}
	}

	private NodeModel targetFromCellContent(final NodeModel content, final NodeModel cell, final Direction direction) {
		final NodeModel row = cell.getParentNode();
		final int columnIndex = columnKeysOf(row).indexOf(TreeTableCells.keyOf(cell));
		switch (direction) {
		case RIGHT:
			return shownChildrenOf(content).isEmpty() ? firstContentOfCellAfter(row, columnIndex) : null;
		case LEFT:
			if (content.getParentNode() != cell)
				return null;
			final NodeModel previous = firstContentOfCellBefore(row, columnIndex);
			return previous != null ? previous : row;
		case UP:
			return isFirstShownIn(content, cell) ? contentInAdjacentRow(row, cell, -1) : null;
		case DOWN:
			return isLastShownIn(content, cell) ? contentInAdjacentRow(row, cell, 1) : null;
		default:
			return null;
		}
	}

	private NodeModel contentInAdjacentRow(final NodeModel row, final NodeModel cell, final int step) {
		final NodeModel adjacentRow = adjacentRow(row, step);
		if (adjacentRow == null)
			return null;
		final List<NodeModel> contents = shownChildrenOf(TreeTableCells.cellOf(adjacentRow, TreeTableCells.keyOf(cell)));
		if (contents.isEmpty())
			return adjacentRow;
		return step < 0 ? contents.get(contents.size() - 1) : contents.get(0);
	}

	private NodeModel adjacentRow(final NodeModel row, final int step) {
		final List<NodeModel> rows = new ArrayList<NodeModel>();
		for (final NodeModel candidate : TreeTableNodes.rowsOf(TreeTableNodes.treeTableRootOf(row))) {
			if (isShown.test(candidate))
				rows.add(candidate);
		}
		final int index = rows.indexOf(row) + step;
		return index >= 0 && index < rows.size() && rows.contains(row) ? rows.get(index) : null;
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
