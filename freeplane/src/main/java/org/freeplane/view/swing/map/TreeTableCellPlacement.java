package org.freeplane.view.swing.map;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.freeplane.features.treetable.TreeTableCells;
import org.freeplane.features.treetable.TreeTableHeader;

class TreeTableCellPlacement {
	private static final String X_IN_ROW = "treeTableCellXInRow";

	static int xInRowOf(final NodeView cellView) {
		final Object x = cellView.getClientProperty(X_IN_ROW);
		return x instanceof Integer ? (Integer) x : 0;
	}

	static int yInRowOf(final NodeView cellView) {
		return cellView.getMap().getZoomed(TreeTableView.TEXT_PADDING) - contentBoundsOf(cellView).y;
	}

	static boolean isCellView(final Component component) {
		return component instanceof NodeView && TreeTableCells.isCell(((NodeView) component).getNode());
	}

	static List<NodeView> cellViewsOf(final NodeView rowView) {
		final List<NodeView> cellViews = new ArrayList<NodeView>();
		for (final NodeView child : rowView.getChildrenViews()) {
			if (isCellView(child))
				cellViews.add(child);
		}
		return cellViews;
	}

	static int treeWidthOf(final NodeView rowView) {
		final JComponent content = rowView.getContent();
		int right = content.getX() + content.getWidth() + rowView.getSpaceAround();
		for (final NodeView child : rowView.getChildrenViews()) {
			if (!isCellView(child) && child.isVisible())
				right = Math.max(right, child.getX() + treeWidthOf(child));
		}
		return right;
	}

	static Rectangle contentBoundsOf(final NodeView cellView) {
		Rectangle bounds = null;
		for (final NodeView child : cellView.getChildrenViews()) {
			if (!child.isVisible())
				continue;
			final int spaceAround = child.getSpaceAround();
			final Rectangle childBounds = new Rectangle(child.getX() + spaceAround, child.getY() + spaceAround,
				child.getWidth() - 2 * spaceAround, child.getHeight() - 2 * spaceAround);
			bounds = bounds == null ? childBounds : bounds.union(childBounds);
		}
		return bounds == null ? new Rectangle() : bounds;
	}

	static void place(final NodeView treeTableRootView) {
		final NodeView header = TreeTableView.headerViewOf(treeTableRootView);
		if (header == null)
			return;
		final Map<String, NodeView> columnViews = new HashMap<String, NodeView>();
		for (final NodeView columnView : TreeTableView.distinctColumnViews(header))
			columnViews.put(TreeTableHeader.keyOf(columnView.getNode()), columnView);
		final int padding = treeTableRootView.getMap().getZoomed(TreeTableView.TEXT_PADDING);
		final List<NodeView> rows = new ArrayList<NodeView>();
		TreeTableView.collectRows(treeTableRootView, rows);
		for (final NodeView rowView : rows) {
			for (final NodeView cellView : cellViewsOf(rowView)) {
				final NodeView columnView = columnViews.get(TreeTableCells.keyOf(cellView.getNode()));
				cellView.setVisible(columnView != null);
				if (columnView != null)
					placeCell(treeTableRootView, rowView, cellView, columnView, padding);
			}
		}
	}

	private static void placeCell(final NodeView treeTableRootView, final NodeView rowView, final NodeView cellView,
			final NodeView columnView, final int padding) {
		final int columnLeft = SwingUtilities.convertPoint(columnView.getMainView(), 0, 0, treeTableRootView).x;
		final Point rowOrigin = SwingUtilities.convertPoint(rowView, 0, 0, treeTableRootView);
		final Rectangle content = contentBoundsOf(cellView);
		final int x = columnLeft + padding - rowOrigin.x - content.x;
		final int y = rowView.getContent().getY() + yInRowOf(cellView);
		cellView.putClientProperty(X_IN_ROW, x - rowView.getContent().getX());
		cellView.setLocation(x, y);
		growAncestorsToContain(cellView, treeTableRootView);
	}

	private static void growAncestorsToContain(final Component component, final Component treeTableRootView) {
		for (Component child = component; child != treeTableRootView; child = child.getParent()) {
			final Component parent = child.getParent();
			final int width = Math.max(parent.getWidth(), child.getX() + child.getWidth());
			final int height = Math.max(parent.getHeight(), child.getY() + child.getHeight());
			if (width != parent.getWidth() || height != parent.getHeight())
				parent.setSize(width, height);
		}
	}

	private TreeTableCellPlacement() {
	}
}
