package org.freeplane.features.treetable;

public class TreeTableGeometry {
	private final int treeColumnWidth;
	private final int[] columnWidths;
	private final int[] columnPositions;
	private final int gap;

	public TreeTableGeometry(final int treeColumnWidth, final int[] columnWidths, final int gap) {
		if (treeColumnWidth < 0)
			throw new IllegalArgumentException("tree column width must not be negative");
		if (gap < 0)
			throw new IllegalArgumentException("tree table gap must not be negative");
		for (final int columnWidth : columnWidths) {
			if (columnWidth < 0)
				throw new IllegalArgumentException("tree table column width must not be negative");
		}
		this.treeColumnWidth = treeColumnWidth;
		this.columnWidths = columnWidths.clone();
		this.gap = gap;
		this.columnPositions = calculateColumnPositions();
	}

	private int[] calculateColumnPositions() {
		final int[] positions = new int[columnWidths.length];
		int position = treeColumnWidth;
		for (int index = 0; index < columnWidths.length; index++) {
			position += gap;
			positions[index] = position;
			position += columnWidths[index];
		}
		return positions;
	}

	public int getTreeColumnWidth() {
		return treeColumnWidth;
	}

	public int getColumnCount() {
		return columnWidths.length;
	}

	public int getColumnWidth(final int index) {
		return columnWidths[index];
	}

	public int getColumnPosition(final int index) {
		return columnPositions[index];
	}

	public int getTotalWidth() {
		if (columnWidths.length == 0)
			return treeColumnWidth;
		final int lastIndex = columnWidths.length - 1;
		return columnPositions[lastIndex] + columnWidths[lastIndex];
	}

	public int getColumnIndexAt(final int x) {
		for (int index = 0; index < columnWidths.length; index++) {
			if (x >= columnPositions[index] && x < columnPositions[index] + columnWidths[index])
				return index;
		}
		return -1;
	}
}
