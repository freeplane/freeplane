package org.freeplane.view.swing.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.treetable.TreeTableColumns;
import org.freeplane.features.treetable.TreeTableGrid;

class TreeTableCellPainter {
	private static final int TEXT_PADDING = 4;
	private static final float THICK_LINE_WIDTH = 2f;
	private static final float THIN_LINE_WIDTH = 1f;
	private static final int THIN_LINE_ALPHA = 96;

	private final MapView mapView;
	private final TreeTableCellEditor cellEditor;

	TreeTableCellPainter(final MapView mapView, final TreeTableCellEditor cellEditor) {
		this.mapView = mapView;
		this.cellEditor = cellEditor;
	}

	void paintTreeTables(final Graphics2D graphics) {
		final NodeView rootView = mapView.getRoot();
		if (rootView == null)
			return;
		final List<NodeView> treeTableRoots = TreeTableView.findTreeTableRoots(rootView, new ArrayList<NodeView>());
		for (final NodeView treeTableRoot : treeTableRoots) {
			final TreeTableView treeTable = TreeTableView.of(mapView, treeTableRoot);
			if (treeTable != null)
				paintTreeTable(graphics, treeTable);
		}
	}

	private void paintTreeTable(final Graphics2D graphics, final TreeTableView treeTable) {
		graphics.setColor(mapView.getForeground());
		paintHeaders(graphics, treeTable);
		paintRows(graphics, treeTable);
		paintGrid(graphics, treeTable.getGrid());
	}

	private void paintGrid(final Graphics2D graphics, final TreeTableGrid grid) {
		final Stroke previousStroke = graphics.getStroke();
		final Color previousColor = graphics.getColor();
		graphics.setStroke(new BasicStroke(mapView.getZoom() * THIN_LINE_WIDTH));
		graphics.setColor(translucent(previousColor));
		for (int i = 0; i < grid.getRowSeparatorCount(); i++)
			graphics.drawLine(grid.getLeft(), grid.getRowSeparatorY(i), grid.getRight(), grid.getRowSeparatorY(i));
		graphics.setColor(previousColor);
		graphics.setStroke(new BasicStroke(mapView.getZoom() * THICK_LINE_WIDTH));
		graphics.drawRect(grid.getLeft(), grid.getTop(), grid.getRight() - grid.getLeft(),
			grid.getBottom() - grid.getTop());
		if (grid.hasHeaderSeparator())
			graphics.drawLine(grid.getLeft(), grid.getHeaderSeparatorY(), grid.getRight(), grid.getHeaderSeparatorY());
		for (int i = 0; i < grid.getColumnSeparatorCount(); i++)
			graphics.drawLine(grid.getColumnSeparatorX(i), grid.getTop(), grid.getColumnSeparatorX(i), grid.getBottom());
		graphics.setStroke(previousStroke);
	}

	private static Color translucent(final Color color) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), THIN_LINE_ALPHA);
	}

	private void paintHeaders(final Graphics2D graphics, final TreeTableView treeTable) {
		final TreeTableColumns columns = treeTable.getColumns();
		for (int i = 0; i < columns.size(); i++) {
			final String attributeKey = columns.get(i).getAttributeKey();
			if (!cellEditor.isEditing(treeTable.getTreeTableRoot(), attributeKey))
				paintTextInCell(graphics, attributeKey, treeTable.getHeaderBounds(i));
		}
	}

	private void paintRows(final Graphics2D graphics, final TreeTableView treeTable) {
		final TreeTableColumns columns = treeTable.getColumns();
		final List<NodeView> rows = treeTable.getRows();
		for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
			final NodeModel row = rows.get(rowIndex).getNode();
			final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(row);
			for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
				final String attributeKey = columns.get(columnIndex).getAttributeKey();
				if (cellEditor.isEditing(row, attributeKey))
					continue;
				final String value = cellValue(attributes, attributeKey);
				if (value != null)
					paintTextInCell(graphics, value, treeTable.getCellBounds(rowIndex, columnIndex));
			}
		}
	}

	private void paintTextInCell(final Graphics2D graphics, final String text, final Rectangle bounds) {
		final FontMetrics metrics = graphics.getFontMetrics();
		final int baseline = bounds.y + (bounds.height + metrics.getAscent() - metrics.getDescent()) / 2;
		final Shape previousClip = graphics.getClip();
		graphics.clipRect(bounds.x, bounds.y, bounds.width, bounds.height);
		graphics.drawString(text, bounds.x + TEXT_PADDING, baseline);
		graphics.setClip(previousClip);
	}

	private String cellValue(final NodeAttributeTableModel attributes, final String attributeKey) {
		final int index = attributes.getAttributeIndex(attributeKey);
		if (index < 0)
			return null;
		final Object value = attributes.getValue(index);
		return value != null ? value.toString() : null;
	}
}
