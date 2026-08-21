package org.freeplane.features.treetable;

public class TreeColumnWidthCalculator {
	private int width;

	public void addRow(final int indentation, final int contentWidth) {
		if (indentation < 0)
			throw new IllegalArgumentException("tree table row indentation must not be negative");
		if (contentWidth < 0)
			throw new IllegalArgumentException("tree table row content width must not be negative");
		width = Math.max(width, indentation + contentWidth);
	}

	public int getWidth() {
		return width;
	}
}
