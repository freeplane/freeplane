package org.freeplane.features.treetable;

public class TreeTableGrid {
	private final int left;
	private final int top;
	private final int[] columnBounds;
	private final int headerBottom;
	private final int[] rowBottoms;
	private final int[] innerSeparatorTops;

	public static int borderLineStart(final int position, final int borderWidth) {
		return position + borderWidth / 2;
	}

	public static int underlineY(final int position, final int size, final int borderWidth) {
		return position + size - 1 - borderWidth / 2;
	}

	public static int borderLineEnd(final int position, final int size, final int borderWidth) {
		return position + size - borderWidth + borderWidth / 2;
	}

	public static int leftOfContent(final int contentX, final int halfGap, final int textPadding) {
		return contentX - halfGap - textPadding;
	}

	public static int[] columnBounds(final int[] lefts, final int[] rights, final int halfGap) {
		if (lefts.length == 0 || lefts.length != rights.length)
			throw new IllegalArgumentException("tree table columns require matching non empty extents");
		final int[] bounds = new int[lefts.length + 1];
		bounds[0] = lefts[0] - halfGap;
		for (int index = 1; index < lefts.length; index++)
			bounds[index] = (rights[index - 1] + lefts[index]) / 2;
		bounds[lefts.length] = rights[rights.length - 1] + halfGap;
		return bounds;
	}

	public TreeTableGrid(final int left, final int top, final int[] columnBounds, final int headerBottom,
			final int[] rowBottoms, final int[] innerSeparatorTops) {
		if (columnBounds.length < 2 || innerSeparatorTops.length != columnBounds.length - 2)
			throw new IllegalArgumentException("tree table grid requires consistent column bounds");
		this.left = left;
		this.top = top;
		this.columnBounds = columnBounds.clone();
		this.headerBottom = headerBottom;
		this.rowBottoms = rowBottoms.clone();
		this.innerSeparatorTops = innerSeparatorTops.clone();
	}

	public int getLeft() {
		return left;
	}

	public int getTop() {
		return top;
	}

	public int getRight() {
		return columnBounds[columnBounds.length - 1];
	}

	public int getBottom() {
		return rowBottoms.length > 0 ? rowBottoms[rowBottoms.length - 1] : headerBottom;
	}

	public int getHeaderBottom() {
		return headerBottom;
	}

	public int getColumnCount() {
		return columnBounds.length - 1;
	}

	public int getColumnLeft(final int columnIndex) {
		return columnBounds[columnIndex];
	}

	public int getColumnRight(final int columnIndex) {
		return columnBounds[columnIndex + 1];
	}

	public int getColumnIndexAt(final int x) {
		for (int index = 0; index < getColumnCount(); index++) {
			if (x >= getColumnLeft(index) && x < getColumnRight(index))
				return index;
		}
		return -1;
	}

	public int getColumnIndexWithResizeEdgeNear(final int x, final int tolerance) {
		for (int index = 0; index < getColumnCount(); index++) {
			if (Math.abs(x - getColumnRight(index)) <= tolerance)
				return index;
		}
		return -1;
	}

	public int getRowCount() {
		return rowBottoms.length;
	}

	public int getRowBottomY(final int rowIndex) {
		return rowBottoms[rowIndex];
	}

	public int getColumnSeparatorCount() {
		return getColumnCount();
	}

	public int getColumnSeparatorX(final int index) {
		return columnBounds[index];
	}

	public int getColumnSeparatorTop(final int index) {
		return index == 0 ? top : innerSeparatorTops[index - 1];
	}
}
