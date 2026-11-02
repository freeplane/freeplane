package org.freeplane.view.swing.map;

import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.treetable.TreeTableCellLayout;
import org.freeplane.features.treetable.TreeTableHeader;
import org.freeplane.features.treetable.TreeTableModel;
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
		final NodeModel treeTableRoot = TreeTableNodes.treeTableRootOf(view.getNode());
		int requiredHeight = requiredCellHeight(view);
		if (TreeTableModel.wrapsText(treeTableRoot))
			requiredHeight = Math.max(requiredHeight, requiredHeight(view, treeTableRoot));
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

	private static int requiredHeight(final NodeView row, final NodeModel treeTableRoot) {
		final MapView map = row.getMap();
		final int textPadding = map.getZoomed(TreeTableView.TEXT_PADDING);
		final Font font = TreeTableCellText.zoomedFontOf(row);
		final List<NodeModel> columnNodes = TreeTableHeader.distinctColumnNodesOf(treeTableRoot);
		final int[] textHeights = new int[columnNodes.size()];
		for (int index = 0; index < textHeights.length; index++) {
			final NodeModel columnNode = columnNodes.get(index);
			final String value = TreeTableCellText.valueOf(row.getNode(), TreeTableHeader.keyOf(columnNode));
			final NodeView columnView = map.getNodeView(columnNode);
			if (value != null && columnView != null && columnView.isContentVisible())
				textHeights[index] = TreeTableCellText.height(value, font, TreeTableCellLayout
					.wrapWidth(columnView.getMainView().getPreferredSize().width, textPadding));
		}
		return TreeTableCellLayout.requiredRowHeight(textHeights, textPadding);
	}

	private TreeTableRowSize() {
	}
}
