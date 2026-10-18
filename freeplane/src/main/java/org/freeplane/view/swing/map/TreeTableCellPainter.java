package org.freeplane.view.swing.map;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.List;

import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.treetable.TreeTableColumns;
import org.freeplane.features.treetable.TreeTableGrid;

class TreeTableCellPainter {
	private static final int TEXT_PADDING = 4;

	private final MapView mapView;
	private final TreeTableCellEditor cellEditor;

	TreeTableCellPainter(final MapView mapView, final TreeTableCellEditor cellEditor) {
		this.mapView = mapView;
		this.cellEditor = cellEditor;
	}

	void paintTreeTables(final Graphics2D graphics) {
		for (final TreeTableView treeTable : TreeTableView.allIn(mapView))
			paintTreeTable(graphics, treeTable);
	}

	private void paintTreeTable(final Graphics2D graphics, final TreeTableView treeTable) {
		final Font previousFont = graphics.getFont();
		final Color previousColor = graphics.getColor();
		paintHeaders(graphics, treeTable);
		paintRows(graphics, treeTable);
		graphics.setFont(previousFont);
		graphics.setColor(previousColor);
		paintGrid(graphics, treeTable);
	}

	private void paintGrid(final Graphics2D graphics, final TreeTableView treeTable) {
		final TreeTableGrid grid = treeTable.getGrid();
		final Stroke previousStroke = graphics.getStroke();
		final Color previousColor = graphics.getColor();
		useLineStyle(graphics, TreeTableLineStyle.ofBorder(treeTable.getTreeTableRootView().getMainView()));
		for (int i = 0; i < grid.getRowSeparatorCount(); i++)
			graphics.drawLine(grid.getLeft(), grid.getRowSeparatorY(i), grid.getRight(), grid.getRowSeparatorY(i));
		graphics.drawRect(grid.getLeft(), grid.getTop(), grid.getRight() - grid.getLeft(),
			grid.getBottom() - grid.getTop());
		if (grid.hasHeaderSeparator())
			graphics.drawLine(grid.getLeft(), grid.getHeaderSeparatorY(), grid.getRight(), grid.getHeaderSeparatorY());
		for (int i = 0; i < grid.getColumnSeparatorCount(); i++)
			graphics.drawLine(grid.getColumnSeparatorX(i), grid.getTop(), grid.getColumnSeparatorX(i), grid.getBottom());
		graphics.setStroke(previousStroke);
		graphics.setColor(previousColor);
	}

	private void useLineStyle(final Graphics2D graphics, final TreeTableLineStyle lineStyle) {
		graphics.setColor(lineStyle.getColor());
		graphics.setStroke(lineStyle.stroke(mapView.getZoom()));
	}

	private void useTextStyle(final Graphics2D graphics, final NodeView view) {
		graphics.setFont(view.getMainView().getFont());
		graphics.setColor(view.getTextColor(view.getStyleOption()));
	}

	private void paintHeaders(final Graphics2D graphics, final TreeTableView treeTable) {
		final TreeTableColumns columns = treeTable.getColumns();
		useTextStyle(graphics, treeTable.getTreeTableRootView());
		for (int i = 0; i < columns.size(); i++) {
			final String attributeKey = columns.get(i).getAttributeKey();
			if (!cellEditor.isEditing(treeTable.getTreeTableRoot(), attributeKey))
				paintTextInCell(graphics, attributeKey, treeTable.getHeaderTextBounds(i));
			paintHeaderMenuButton(graphics, treeTable.getHeaderMenuButtonBounds(i));
		}
	}

	private void paintHeaderMenuButton(final Graphics2D graphics, final Rectangle bounds) {
		final FontMetrics metrics = graphics.getFontMetrics();
		final int textX = bounds.x + (bounds.width - metrics.stringWidth(TreeTableHeaderButton.LABEL)) / 2;
		final int baseline = bounds.y + (bounds.height + metrics.getAscent() - metrics.getDescent()) / 2;
		graphics.drawString(TreeTableHeaderButton.LABEL, textX, baseline);
	}

	private void paintRows(final Graphics2D graphics, final TreeTableView treeTable) {
		final TreeTableColumns columns = treeTable.getColumns();
		final List<NodeView> rows = treeTable.getRows();
		for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
			final NodeModel row = rows.get(rowIndex).getNode();
			final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(row);
			useTextStyle(graphics, rows.get(rowIndex));
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
