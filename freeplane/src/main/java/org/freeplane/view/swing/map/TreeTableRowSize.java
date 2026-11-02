package org.freeplane.view.swing.map;

import java.awt.Dimension;

import org.freeplane.features.treetable.TreeTableHeader;
import org.freeplane.features.treetable.TreeTableNodes;

class TreeTableRowSize {

	static Dimension preferredSize(final MainView mainView, final Dimension size) {
		final NodeView view = mainView.getNodeView();
		if (view == null)
			return size;
		if (TreeTableHeader.isColumnNode(view.getNode()))
			return TreeTableColumnSize.preferredSize(view, size);
		if (!TreeTableNodes.isRow(view.getNode()))
			return size;
		final int requiredHeight = requiredCellHeight(view);
		return requiredHeight > size.height ? new Dimension(size.width, requiredHeight) : size;
	}

	private static int requiredCellHeight(final NodeView row) {
		final int textPadding = row.getMap().getZoomed(TreeTableView.TEXT_PADDING);
		int requiredHeight = 0;
		for (final NodeView cellView : TreeTableCellPlacement.cellViewsOf(row)) {
			cellView.validate();
			requiredHeight = Math.max(requiredHeight,
				TreeTableCellPlacement.contentBoundsOf(cellView).height + 2 * textPadding);
		}
		return requiredHeight;
	}

	private TreeTableRowSize() {
	}
}
