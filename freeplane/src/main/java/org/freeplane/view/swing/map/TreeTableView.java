package org.freeplane.view.swing.map;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.treetable.TreeColumnWidthCalculator;
import org.freeplane.features.treetable.TreeTableColumns;
import org.freeplane.features.treetable.TreeTableGeometry;
import org.freeplane.features.treetable.TreeTableGrid;
import org.freeplane.features.treetable.TreeTableModel;

class TreeTableView {
	static final int COLUMN_GAP = 6;
	static final int GRID_PADDING = 3;

	private final NodeView treeTableRootView;
	private final NodeModel treeTableRoot;
	private final TreeTableColumns columns;
	private final TreeTableGeometry geometry;
	private final TreeTableGrid grid;
	private final int buttonSize;
	private final Rectangle tableBounds;
	private final List<NodeView> rows;
	private final List<Rectangle> rowBounds;

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
		final TreeTableColumns columns = TreeTableModel.columnsOf(treeTableRoot.getNode());
		if (columns.isEmpty())
			return null;
		final List<NodeView> rows = new ArrayList<NodeView>();
		collectRows(treeTableRoot, rows);
		if (rows.isEmpty())
			return null;
		return new TreeTableView(mapView, treeTableRoot, columns, rows);
	}

	private static void collectRows(final NodeView view, final List<NodeView> rows) {
		for (final NodeView child : view.getChildrenViews()) {
			if (child.isContentVisible())
				rows.add(child);
			collectRows(child, rows);
		}
	}

	private TreeTableView(final MapView mapView, final NodeView treeTableRoot, final TreeTableColumns columns,
			final List<NodeView> rows) {
		this.treeTableRootView = treeTableRoot;
		this.treeTableRoot = treeTableRoot.getNode();
		this.columns = columns;
		this.rows = Collections.unmodifiableList(rows);
		this.tableBounds = contentBoundsInMap(mapView, treeTableRoot);
		this.rowBounds = new ArrayList<Rectangle>(rows.size());

		final TreeColumnWidthCalculator treeColumnWidth = new TreeColumnWidthCalculator();
		treeColumnWidth.addRow(0, tableBounds.width);
		for (final NodeView row : rows) {
			final Rectangle bounds = contentBoundsInMap(mapView, row);
			rowBounds.add(bounds);
			treeColumnWidth.addRow(Math.max(0, bounds.x - tableBounds.x), bounds.width);
		}
		this.geometry = new TreeTableGeometry(treeColumnWidth.getWidth(), zoomedColumnWidths(mapView, columns),
			mapView.getZoomed(COLUMN_GAP));
		this.grid = createGrid(mapView);
		this.buttonSize = mapView.getZoomed(TreeTableHeaderButton.SIZE);
	}

	private TreeTableGrid createGrid(final MapView mapView) {
		final int lineCount = rowBounds.size() + 1;
		final int[] lineTops = new int[lineCount];
		final int[] lineBottoms = new int[lineCount];
		lineTops[0] = tableBounds.y;
		lineBottoms[0] = tableBounds.y + tableBounds.height;
		for (int rowIndex = 0; rowIndex < rowBounds.size(); rowIndex++) {
			final Rectangle row = rowBounds.get(rowIndex);
			lineTops[rowIndex + 1] = row.y;
			lineBottoms[rowIndex + 1] = row.y + row.height;
		}
		return new TreeTableGrid(tableBounds.x, geometry, lineTops, lineBottoms, mapView.getZoomed(GRID_PADDING));
	}

	private static int[] zoomedColumnWidths(final MapView mapView, final TreeTableColumns columns) {
		final int[] widths = new int[columns.size()];
		for (int i = 0; i < columns.size(); i++) {
			final Quantity<LengthUnit> width = columns.get(i).getWidth();
			widths[i] = mapView.getZoomed(width.toBaseUnitsRounded());
		}
		return widths;
	}

	private static Rectangle contentBoundsInMap(final MapView mapView, final NodeView view) {
		final JComponent content = view.getContent();
		final Rectangle bounds = new Rectangle(0, 0, content.getWidth(), content.getHeight());
		return SwingUtilities.convertRectangle(content, bounds, mapView);
	}

	NodeView getTreeTableRootView() {
		return treeTableRootView;
	}

	NodeModel getTreeTableRoot() {
		return treeTableRoot;
	}

	TreeTableColumns getColumns() {
		return columns;
	}

	List<NodeView> getRows() {
		return rows;
	}

	TreeTableGrid getGrid() {
		return grid;
	}

	Rectangle getHeaderBounds(final int columnIndex) {
		return new Rectangle(tableBounds.x + geometry.getColumnPosition(columnIndex), tableBounds.y,
			geometry.getColumnWidth(columnIndex), tableBounds.height);
	}

	Rectangle getHeaderTextBounds(final int columnIndex) {
		return TreeTableHeaderButton.textBoundsIn(getHeaderBounds(columnIndex), buttonSize);
	}

	Rectangle getHeaderMenuButtonBounds(final int columnIndex) {
		return TreeTableHeaderButton.boundsIn(getHeaderBounds(columnIndex), buttonSize);
	}

	int headerMenuColumnAt(final int x, final int y) {
		final int columnIndex = geometry.getColumnIndexAt(x - tableBounds.x);
		if (columnIndex < 0 || !isWithin(y, tableBounds))
			return -1;
		return getHeaderMenuButtonBounds(columnIndex).contains(x, y) ? columnIndex : -1;
	}

	Rectangle getCellBounds(final int rowIndex, final int columnIndex) {
		final Rectangle row = rowBounds.get(rowIndex);
		return new Rectangle(tableBounds.x + geometry.getColumnPosition(columnIndex), row.y,
			geometry.getColumnWidth(columnIndex), row.height);
	}

	TreeTableCell cellAt(final int x, final int y) {
		final int columnIndex = geometry.getColumnIndexAt(x - tableBounds.x);
		if (columnIndex < 0)
			return null;
		final String attributeKey = columns.get(columnIndex).getAttributeKey();
		if (isWithin(y, tableBounds))
			return getHeaderTextBounds(columnIndex).contains(x, y)
					? TreeTableCell.header(treeTableRoot, attributeKey, getHeaderTextBounds(columnIndex))
					: null;
		for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
			if (isWithin(y, rowBounds.get(rowIndex)))
				return TreeTableCell.value(rows.get(rowIndex).getNode(), attributeKey,
					getCellBounds(rowIndex, columnIndex));
		}
		return null;
	}

	private static boolean isWithin(final int y, final Rectangle bounds) {
		return y >= bounds.y && y < bounds.y + bounds.height;
	}
}
