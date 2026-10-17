package org.freeplane.features.treetable;

public class TreeTableGrid {
	private final int left;
	private final int top;
	private final int right;
	private final int bottom;
	private final int[] lineSeparatorYs;
	private final int[] columnSeparatorXs;

	public TreeTableGrid(final int tableX, final TreeTableGeometry geometry, final int[] lineTops,
			final int[] lineBottoms, final int padding) {
		if (lineTops.length == 0 || lineTops.length != lineBottoms.length)
			throw new IllegalArgumentException("tree table grid requires matching non empty line extents");
		if (padding < 0)
			throw new IllegalArgumentException("tree table grid padding must not be negative");
		this.left = tableX - padding;
		this.right = tableX + geometry.getTotalWidth() + padding;
		this.top = lineTops[0] - padding;
		this.bottom = lineBottoms[lineBottoms.length - 1] + padding;
		this.lineSeparatorYs = calculateLineSeparators(lineTops, lineBottoms);
		this.columnSeparatorXs = calculateColumnSeparators(tableX, geometry);
	}

	private static int[] calculateLineSeparators(final int[] lineTops, final int[] lineBottoms) {
		final int[] separators = new int[lineTops.length - 1];
		for (int index = 0; index < separators.length; index++)
			separators[index] = (lineBottoms[index] + lineTops[index + 1]) / 2;
		return separators;
	}

	private static int[] calculateColumnSeparators(final int tableX, final TreeTableGeometry geometry) {
		final int[] separators = new int[geometry.getColumnCount()];
		for (int index = 0; index < separators.length; index++)
			separators[index] = tableX + geometry.getColumnPosition(index) - geometry.getGap() / 2;
		return separators;
	}

	public int getLeft() {
		return left;
	}

	public int getTop() {
		return top;
	}

	public int getRight() {
		return right;
	}

	public int getBottom() {
		return bottom;
	}

	public boolean hasHeaderSeparator() {
		return lineSeparatorYs.length > 0;
	}

	public int getHeaderSeparatorY() {
		return lineSeparatorYs[0];
	}

	public int getRowSeparatorCount() {
		return Math.max(0, lineSeparatorYs.length - 1);
	}

	public int getRowSeparatorY(final int index) {
		return lineSeparatorYs[index + 1];
	}

	public int getColumnSeparatorCount() {
		return columnSeparatorXs.length;
	}

	public int getColumnSeparatorX(final int index) {
		return columnSeparatorXs[index];
	}
}
