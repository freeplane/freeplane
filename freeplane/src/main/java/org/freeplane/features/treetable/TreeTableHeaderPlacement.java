package org.freeplane.features.treetable;

public class TreeTableHeaderPlacement {
	public static final int GAP = 6;

	public static int headerX(final int treeColumnRight, final int gap, final int spaceAround) {
		return treeColumnRight + gap - spaceAround;
	}

	public static int treeColumnRight(final int contentRight, final int[] childXs, final int[] childWidths,
			final int spaceAround) {
		int right = contentRight;
		for (int index = 0; index < childXs.length; index++)
			right = Math.max(right, childXs[index] + childWidths[index] - spaceAround);
		return right;
	}

	private TreeTableHeaderPlacement() {
	}
}
