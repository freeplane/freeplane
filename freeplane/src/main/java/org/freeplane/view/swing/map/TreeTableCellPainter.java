package org.freeplane.view.swing.map;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.treetable.TreeColumnWidthCalculator;
import org.freeplane.features.treetable.TreeTableColumn;
import org.freeplane.features.treetable.TreeTableColumns;
import org.freeplane.features.treetable.TreeTableGeometry;
import org.freeplane.features.treetable.TreeTableModel;

class TreeTableCellPainter {
	private static final int COLUMN_GAP = 6;
	private static final int TEXT_PADDING = 4;

	private final MapView mapView;

	TreeTableCellPainter(final MapView mapView) {
		this.mapView = mapView;
	}

	void paintTreeTables(final Graphics2D graphics) {
		final NodeView rootView = mapView.getRoot();
		if (rootView == null)
			return;
		final List<NodeView> treeTableRoots = new ArrayList<NodeView>();
		collectTreeTableRoots(rootView, treeTableRoots);
		for (final NodeView treeTableRoot : treeTableRoots)
			paintTreeTable(graphics, treeTableRoot);
	}

	private void collectTreeTableRoots(final NodeView view, final List<NodeView> treeTableRoots) {
		if (TreeTableModel.isTreeTableRoot(view.getNode()))
			treeTableRoots.add(view);
		for (final NodeView child : view.getChildrenViews())
			collectTreeTableRoots(child, treeTableRoots);
	}

	private void paintTreeTable(final Graphics2D graphics, final NodeView treeTableRoot) {
		final TreeTableColumns columns = TreeTableModel.columnsOf(treeTableRoot.getNode());
		if (columns.isEmpty())
			return;
		final List<NodeView> rows = new ArrayList<NodeView>();
		collectRows(treeTableRoot, rows);
		if (rows.isEmpty())
			return;

		final Rectangle tableBounds = contentBoundsInMap(treeTableRoot);
		final TreeColumnWidthCalculator treeColumnWidth = new TreeColumnWidthCalculator();
		final List<Rectangle> rowBounds = new ArrayList<Rectangle>();
		for (final NodeView row : rows) {
			final Rectangle bounds = contentBoundsInMap(row);
			rowBounds.add(bounds);
			treeColumnWidth.addRow(Math.max(0, bounds.x - tableBounds.x), bounds.width);
		}

		final TreeTableGeometry geometry = new TreeTableGeometry(treeColumnWidth.getWidth(),
			zoomedColumnWidths(columns), mapView.getZoomed(COLUMN_GAP));

		final Shape previousClip = graphics.getClip();
		paintHeaders(graphics, columns, geometry, tableBounds);
		for (int i = 0; i < rows.size(); i++)
			paintRow(graphics, rows.get(i), rowBounds.get(i), columns, geometry, tableBounds.x);
		graphics.setClip(previousClip);
	}

	private int[] zoomedColumnWidths(final TreeTableColumns columns) {
		final int[] widths = new int[columns.size()];
		for (int i = 0; i < columns.size(); i++) {
			final Quantity<LengthUnit> width = columns.get(i).getWidth();
			widths[i] = mapView.getZoomed(width.toBaseUnitsRounded());
		}
		return widths;
	}

	private void paintHeaders(final Graphics2D graphics, final TreeTableColumns columns,
			final TreeTableGeometry geometry, final Rectangle tableBounds) {
		final FontMetrics metrics = graphics.getFontMetrics();
		final int baseline = tableBounds.y + metrics.getAscent();
		graphics.setColor(headerColor());
		for (int i = 0; i < columns.size(); i++) {
			final TreeTableColumn column = columns.get(i);
			final int x = tableBounds.x + geometry.getColumnPosition(i);
			paintClippedText(graphics, column.getAttributeKey(), x, baseline, geometry.getColumnWidth(i));
		}
	}

	private void paintRow(final Graphics2D graphics, final NodeView row, final Rectangle rowBounds,
			final TreeTableColumns columns, final TreeTableGeometry geometry, final int tableLeft) {
		final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(row.getNode());
		final FontMetrics metrics = graphics.getFontMetrics();
		final int baseline = rowBounds.y + (rowBounds.height + metrics.getAscent() - metrics.getDescent()) / 2;
		graphics.setColor(cellColor());
		for (int i = 0; i < columns.size(); i++) {
			final String value = cellValue(attributes, columns.get(i).getAttributeKey());
			if (value == null)
				continue;
			final int x = tableLeft + geometry.getColumnPosition(i);
			paintClippedText(graphics, value, x, baseline, geometry.getColumnWidth(i));
		}
	}

	private void paintClippedText(final Graphics2D graphics, final String text, final int x, final int baseline,
			final int width) {
		final Shape previousClip = graphics.getClip();
		graphics.clipRect(x, baseline - graphics.getFontMetrics().getAscent() - TEXT_PADDING,
			width, graphics.getFontMetrics().getHeight() + 2 * TEXT_PADDING);
		graphics.drawString(text, x + TEXT_PADDING, baseline);
		graphics.setClip(previousClip);
	}

	private String cellValue(final NodeAttributeTableModel attributes, final String attributeKey) {
		final int index = attributes.getAttributeIndex(attributeKey);
		if (index < 0)
			return null;
		final Object value = attributes.getValue(index);
		return value != null ? value.toString() : null;
	}

	private Rectangle contentBoundsInMap(final NodeView view) {
		final JComponent content = view.getContent();
		final Rectangle bounds = new Rectangle(0, 0, content.getWidth(), content.getHeight());
		return SwingUtilities.convertRectangle(content, bounds, mapView);
	}

	private void collectRows(final NodeView view, final List<NodeView> rows) {
		for (final NodeView child : view.getChildrenViews()) {
			if (child.isContentVisible())
				rows.add(child);
			collectRows(child, rows);
		}
	}

	private Color headerColor() {
		return mapView.getForeground();
	}

	private Color cellColor() {
		return mapView.getForeground();
	}

	static boolean hasTreeTable(final NodeModel node) {
		return TreeTableModel.isTreeTableRoot(node);
	}
}
