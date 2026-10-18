package org.freeplane.view.swing.map;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.nodestyle.NodeStyleShape;
import org.freeplane.features.treetable.TreeTableCellLayout;
import org.freeplane.features.treetable.TreeTableColumn;
import org.freeplane.features.treetable.TreeTableColumns;
import org.freeplane.features.treetable.TreeTableGrid;
import org.freeplane.features.treetable.TreeTableHeader;
import org.freeplane.features.treetable.TreeTableHeaderPlacement;
import org.freeplane.features.treetable.TreeTableModel;

class TreeTableView {
	static final int TEXT_PADDING = 4;

	static class HeaderLine {
		final NodeView view;
		final int y;
		final int left;
		final int right;

		private HeaderLine(final NodeView view, final int y, final int left, final int right) {
			this.view = view;
			this.y = y;
			this.left = left;
			this.right = right;
		}
	}

	private final MapView mapView;
	private final NodeView treeTableRootView;
	private final List<NodeView> rows;
	private final List<Rectangle> rowBounds;
	private final List<NodeView> columnViews;
	private final List<Rectangle> columnBounds;
	private final TreeTableColumns columns;
	private final List<HeaderLine> headerLines;
	private final TreeTableGrid grid;
	private final int textPadding;

	static List<TreeTableView> allIn(final MapView mapView) {
		final List<TreeTableView> treeTables = new ArrayList<TreeTableView>();
		final NodeView rootView = mapView.getRoot();
		if (rootView == null)
			return treeTables;
		for (final NodeView treeTableRoot : findTreeTableRoots(rootView, new ArrayList<NodeView>())) {
			final TreeTableView treeTable = of(mapView, treeTableRoot);
			if (treeTable != null)
				treeTables.add(treeTable);
		}
		return treeTables;
	}

	static List<NodeView> findTreeTableRoots(final NodeView view, final List<NodeView> treeTableRoots) {
		if (TreeTableModel.isTreeTableRoot(view.getNode()))
			treeTableRoots.add(view);
		for (final NodeView child : view.getChildrenViews())
			findTreeTableRoots(child, treeTableRoots);
		return treeTableRoots;
	}

	static TreeTableView of(final MapView mapView, final NodeView treeTableRoot) {
		final NodeView header = headerViewOf(treeTableRoot);
		if (header == null)
			return null;
		final List<NodeView> columnViews = distinctColumnViews(header);
		if (columnViews.isEmpty())
			return null;
		final List<NodeView> rows = new ArrayList<NodeView>();
		collectRows(treeTableRoot, rows);
		return new TreeTableView(mapView, treeTableRoot, header, columnViews, rows);
	}

	private static NodeView headerViewOf(final NodeView treeTableRoot) {
		for (final NodeView child : treeTableRoot.getChildrenViews()) {
			if (child.isContentVisible() && TreeTableHeader.isHeaderRoot(child.getNode()))
				return child;
		}
		return null;
	}

	private static List<NodeView> distinctColumnViews(final NodeView header) {
		final List<NodeView> leaves = new ArrayList<NodeView>();
		for (final NodeView child : visibleChildrenOf(header))
			collectLeaves(child, leaves);
		final List<NodeView> distinct = new ArrayList<NodeView>();
		final Set<String> keys = new HashSet<String>();
		for (final NodeView leaf : leaves) {
			final String key = TreeTableHeader.keyOf(leaf.getNode());
			if (!key.isEmpty() && keys.add(key))
				distinct.add(leaf);
		}
		return distinct;
	}

	private static void collectLeaves(final NodeView view, final List<NodeView> leaves) {
		final List<NodeView> children = visibleChildrenOf(view);
		if (children.isEmpty())
			leaves.add(view);
		for (final NodeView child : children)
			collectLeaves(child, leaves);
	}

	private static void collectHeaderViews(final NodeView view, final List<NodeView> headerViews) {
		headerViews.add(view);
		for (final NodeView child : visibleChildrenOf(view))
			collectHeaderViews(child, headerViews);
	}

	private static List<NodeView> visibleChildrenOf(final NodeView view) {
		final List<NodeView> children = new ArrayList<NodeView>();
		for (final NodeView child : view.getChildrenViews()) {
			if (child.isContentVisible())
				children.add(child);
		}
		return children;
	}

	private static void collectRows(final NodeView view, final List<NodeView> rows) {
		for (final NodeView child : view.getChildrenViews()) {
			if (TreeTableHeader.isHeaderRoot(child.getNode()))
				continue;
			if (child.isContentVisible())
				rows.add(child);
			collectRows(child, rows);
		}
	}

	private TreeTableView(final MapView mapView, final NodeView treeTableRoot, final NodeView header,
			final List<NodeView> columnViews, final List<NodeView> rows) {
		this.mapView = mapView;
		this.treeTableRootView = treeTableRoot;
		this.rows = Collections.unmodifiableList(rows);
		this.columnViews = Collections.unmodifiableList(columnViews);
		this.textPadding = mapView.getZoomed(TEXT_PADDING);
		this.rowBounds = boundsOf(rows);
		this.columnBounds = boundsOf(columnViews);
		this.columns = columnsOf(columnViews);

		final List<NodeView> headerViews = new ArrayList<NodeView>();
		collectHeaderViews(header, headerViews);
		final int halfGap = mapView.getZoomed(TreeTableHeaderPlacement.GAP) / 2;
		final Rectangle rootBounds = mainViewBoundsInMap(treeTableRoot);
		final int top = TreeTableGrid.borderLineStart(rootBounds.y, borderWidthOf(treeTableRoot));
		final int left = TreeTableGrid.leftOfContent(rootBounds.x + contentStartOf(treeTableRoot.getMainView()),
			halfGap, textPadding);
		this.grid = new TreeTableGrid(left, top,
			TreeTableGrid.columnBounds(lefts(columnBounds), rights(columnBounds), halfGap),
			headerBottomOf(treeTableRoot, headerViews), rowBottoms(), innerSeparatorTops(headerViews, top));
		this.headerLines = headerLinesOf(headerViews);
	}

	private int headerBottomOf(final NodeView treeTableRoot, final List<NodeView> headerViews) {
		int headerBottom = bottomBorderLineOf(treeTableRoot);
		for (final NodeView headerView : headerViews)
			headerBottom = Math.max(headerBottom, bottomBorderLineOf(headerView));
		return headerBottom;
	}

	private int[] rowBottoms() {
		final int[] rowBottoms = new int[rows.size()];
		for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++)
			rowBottoms[rowIndex] = bottomBorderLineOf(rows.get(rowIndex), rowBounds.get(rowIndex));
		return rowBottoms;
	}

	private int[] innerSeparatorTops(final List<NodeView> headerViews, final int top) {
		final Map<NodeModel, NodeView> headerViewsByNode = new HashMap<NodeModel, NodeView>();
		for (final NodeView headerView : headerViews)
			headerViewsByNode.put(headerView.getNode(), headerView);
		final int[] tops = new int[columnViews.size() - 1];
		for (int index = 1; index < columnViews.size(); index++) {
			final NodeModel commonAncestor = TreeTableHeader.lowestCommonAncestor(
				columnViews.get(index - 1).getNode(), columnViews.get(index).getNode());
			final NodeView commonAncestorView = headerViewsByNode.get(commonAncestor);
			tops[index - 1] = commonAncestorView != null ? bottomBorderLineOf(commonAncestorView) : top;
		}
		return tops;
	}

	private List<HeaderLine> headerLinesOf(final List<NodeView> headerViews) {
		final List<HeaderLine> lines = new ArrayList<HeaderLine>();
		for (final NodeView headerView : headerViews) {
			int first = -1;
			int last = -1;
			for (int index = 0; index < columnViews.size(); index++) {
				final NodeModel columnNode = columnViews.get(index).getNode();
				if (columnNode == headerView.getNode() || columnNode.isDescendantOf(headerView.getNode())) {
					if (first < 0)
						first = index;
					last = index;
				}
			}
			if (first >= 0)
				lines.add(new HeaderLine(headerView, bottomBorderLineOf(headerView), grid.getColumnLeft(first),
					grid.getColumnRight(last)));
		}
		return lines;
	}

	private static TreeTableColumns columnsOf(final List<NodeView> columnViews) {
		final TreeTableColumn[] columns = new TreeTableColumn[columnViews.size()];
		for (int index = 0; index < columns.length; index++)
			columns[index] = new TreeTableColumn(TreeTableHeader.keyOf(columnViews.get(index).getNode()));
		return TreeTableColumns.of(columns);
	}

	private List<Rectangle> boundsOf(final List<NodeView> views) {
		final List<Rectangle> bounds = new ArrayList<Rectangle>(views.size());
		for (final NodeView view : views)
			bounds.add(mainViewBoundsInMap(view));
		return bounds;
	}

	private static int[] lefts(final List<Rectangle> bounds) {
		final int[] lefts = new int[bounds.size()];
		for (int index = 0; index < lefts.length; index++)
			lefts[index] = bounds.get(index).x;
		return lefts;
	}

	private static int[] rights(final List<Rectangle> bounds) {
		final int[] rights = new int[bounds.size()];
		for (int index = 0; index < rights.length; index++)
			rights[index] = bounds.get(index).x + bounds.get(index).width;
		return rights;
	}

	private static int contentStartOf(final MainView mainView) {
		final ZoomableLabelUI ui = (ZoomableLabelUI) mainView.getUI();
		final int textX = ui.getTextR(mainView).x;
		final Rectangle iconR = ui.getIconR(mainView);
		return iconR.width > 0 ? Math.min(textX, iconR.x) : textX;
	}

	private int bottomBorderLineOf(final NodeView view) {
		return bottomBorderLineOf(view, mainViewBoundsInMap(view));
	}

	private static int bottomBorderLineOf(final NodeView view, final Rectangle bounds) {
		final int borderWidth = borderWidthOf(view);
		return isUnderlined(view)
				? TreeTableGrid.underlineY(bounds.y, bounds.height, borderWidth)
				: TreeTableGrid.borderLineEnd(bounds.y, bounds.height, borderWidth);
	}

	private static boolean isUnderlined(final NodeView view) {
		return view.getMainView().getShapeConfiguration().getShape() == NodeStyleShape.fork;
	}

	private static int borderWidthOf(final NodeView view) {
		return view.getMainView().getPaintedBorderWidth();
	}

	private Rectangle mainViewBoundsInMap(final NodeView view) {
		final JComponent content = view.getMainView();
		final Rectangle bounds = new Rectangle(0, 0, content.getWidth(), content.getHeight());
		return SwingUtilities.convertRectangle(content, bounds, mapView);
	}

	int getTextPadding() {
		return textPadding;
	}

	NodeView getTreeTableRootView() {
		return treeTableRootView;
	}

	TreeTableColumns getColumns() {
		return columns;
	}

	NodeModel getColumnNode(final int columnIndex) {
		return columnViews.get(columnIndex).getNode();
	}

	int getColumnNodeLeft(final int columnIndex) {
		return columnBounds.get(columnIndex).x;
	}

	int getWrapWidth(final int columnIndex) {
		return TreeTableModel.wrapsText(treeTableRootView.getNode())
				? TreeTableCellLayout.wrapWidth(columnBounds.get(columnIndex).width, textPadding)
				: -1;
	}

	List<NodeView> getRows() {
		return rows;
	}

	TreeTableGrid getGrid() {
		return grid;
	}

	List<HeaderLine> getHeaderLines() {
		return headerLines;
	}

	int columnResizeEdgeAt(final int x, final int y, final int tolerance) {
		if (y < grid.getTop() || y > grid.getBottom())
			return -1;
		return grid.getColumnIndexWithResizeEdgeNear(x, tolerance);
	}

	Rectangle getCellBounds(final int rowIndex, final int columnIndex) {
		final Rectangle row = rowBounds.get(rowIndex);
		return new Rectangle(grid.getColumnLeft(columnIndex), row.y,
			grid.getColumnRight(columnIndex) - grid.getColumnLeft(columnIndex), row.height);
	}

	TreeTableCell cellAt(final int x, final int y) {
		final int columnIndex = grid.getColumnIndexAt(x);
		if (columnIndex < 0)
			return null;
		final String attributeKey = columns.get(columnIndex).getAttributeKey();
		for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
			if (isWithin(y, rowBounds.get(rowIndex)))
				return new TreeTableCell(rows.get(rowIndex).getNode(), attributeKey,
					getCellBounds(rowIndex, columnIndex));
		}
		return null;
	}

	private static boolean isWithin(final int y, final Rectangle bounds) {
		return y >= bounds.y && y < bounds.y + bounds.height;
	}
}
