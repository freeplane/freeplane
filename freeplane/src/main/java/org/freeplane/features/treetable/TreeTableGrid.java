package org.freeplane.features.treetable;

public class TreeTableGrid {
	private final int left;
	private final int top;
	private final int right;
	private final int bottom;
	private final int[] lineTops;
	private final int[] lineBottoms;
	private final int[] columnSeparatorXs;

	public static int borderLineStart(final int position, final int borderWidth) {
		return position + borderWidth / 2;
	}

	public static int borderLineEnd(final int position, final int size, final int borderWidth) {
		return position + size - borderWidth + borderWidth / 2;
	}

	public TreeTableGrid(final int tableX, final TreeTableGeometry geometry, final int[] lineTops,
			final int[] lineBottoms) {
		if (lineTops.length == 0 || lineTops.length != lineBottoms.length)
			throw new IllegalArgumentException("tree table grid requires matching non empty line extents");
		this.left = tableX;
		this.right = tableX + geometry.getTotalWidth();
		this.top = lineTops[0];
		this.bottom = lineBottoms[lineBottoms.length - 1];
		this.lineTops = lineTops.clone();
		this.lineBottoms = lineBottoms.clone();
		this.columnSeparatorXs = calculateColumnSeparators(tableX, geometry);
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
		return lineTops.length > 1;
	}

	public int getHeaderSeparatorY() {
		return lineBottoms[0];
	}

	public int getRowCount() {
		return lineTops.length - 1;
	}

	public int getRowTopY(final int rowIndex) {
		return lineTops[rowIndex + 1];
	}

	public int getRowBottomY(final int rowIndex) {
		return lineBottoms[rowIndex + 1];
	}

	public int getColumnSeparatorCount() {
		return columnSeparatorXs.length;
	}

	public int getColumnSeparatorX(final int index) {
		return columnSeparatorXs[index];
	}
}
