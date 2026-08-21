package org.freeplane.view.swing.map;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.features.treetable.TreeColumnWidthCalculator;
import org.freeplane.features.treetable.TreeTableColumns;
import org.freeplane.features.treetable.TreeTableGeometry;
import org.freeplane.features.treetable.TreeTableModel;

class TreeTableView {
	static final int COLUMN_GAP = 6;

	private final TreeTableColumns columns;
	private final TreeTableGeometry geometry;
	private final Rectangle tableBounds;
	private final List<NodeView> rows;
	private final List<Rectangle> rowBounds;

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
		this.columns = columns;
		this.rows = Collections.unmodifiableList(rows);
		this.tableBounds = contentBoundsInMap(mapView, treeTableRoot);
		this.rowBounds = new ArrayList<Rectangle>(rows.size());

		final TreeColumnWidthCalculator treeColumnWidth = new TreeColumnWidthCalculator();
		for (final NodeView row : rows) {
			final Rectangle bounds = contentBoundsInMap(mapView, row);
			rowBounds.add(bounds);
			treeColumnWidth.addRow(Math.max(0, bounds.x - tableBounds.x), bounds.width);
		}
		this.geometry = new TreeTableGeometry(treeColumnWidth.getWidth(), zoomedColumnWidths(mapView, columns),
			mapView.getZoomed(COLUMN_GAP));
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

	TreeTableColumns getColumns() {
		return columns;
	}

	List<NodeView> getRows() {
		return rows;
	}

	Rectangle getHeaderBounds(final int columnIndex) {
		return new Rectangle(tableBounds.x + geometry.getColumnPosition(columnIndex), tableBounds.y,
			geometry.getColumnWidth(columnIndex), tableBounds.height);
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
		for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
			final Rectangle row = rowBounds.get(rowIndex);
			if (y >= row.y && y < row.y + row.height)
				return new TreeTableCell(rows.get(rowIndex).getNode(),
					columns.get(columnIndex).getAttributeKey(), getCellBounds(rowIndex, columnIndex));
		}
		return null;
	}
}
