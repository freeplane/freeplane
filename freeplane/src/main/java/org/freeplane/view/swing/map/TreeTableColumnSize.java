package org.freeplane.view.swing.map;

import java.awt.Dimension;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.treetable.TreeTableCells;
import org.freeplane.features.treetable.TreeTableHeader;
import org.freeplane.features.treetable.TreeTableNodes;

class TreeTableColumnSize {

	static Dimension preferredSize(final NodeView columnView, final Dimension size) {
		final int requiredWidth = Math.min(requiredWidth(columnView), maxWidthOf(columnView));
		return requiredWidth > size.width ? new Dimension(requiredWidth, size.height) : size;
	}

	private static int requiredWidth(final NodeView columnView) {
		final NodeModel columnNode = columnView.getNode();
		final NodeModel treeTableRoot = TreeTableNodes.treeTableRootOf(columnNode);
		final MapView map = columnView.getMap();
		final String key = TreeTableHeader.keyOf(columnNode);
		final int textPadding = map.getZoomed(TreeTableView.TEXT_PADDING);
		int requiredWidth = 0;
		for (final NodeModel row : TreeTableNodes.rowsOf(treeTableRoot)) {
			final NodeModel cell = TreeTableCells.cellOf(row, key);
			final NodeView cellView = cell == null ? null : map.getNodeView(cell);
			if (cellView != null) {
				cellView.validate();
				requiredWidth = Math.max(requiredWidth,
					TreeTableCellPlacement.contentBoundsOf(cellView).width + 2 * textPadding);
			}
		}
		return requiredWidth;
	}

	private static int maxWidthOf(final NodeView columnView) {
		return columnView.getMap().getZoomed(NodeStyleController.getController()
			.getMaxWidth(columnView.getNode(), columnView.getStyleOption()).toBaseUnits());
	}

	private TreeTableColumnSize() {
	}
}
