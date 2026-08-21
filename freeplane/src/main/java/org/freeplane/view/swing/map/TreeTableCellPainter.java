package org.freeplane.view.swing.map;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.treetable.TreeTableColumns;

class TreeTableCellPainter {
	private static final int TEXT_PADDING = 4;

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
	}

	private void paintHeaders(final Graphics2D graphics, final TreeTableView treeTable) {
		final TreeTableColumns columns = treeTable.getColumns();
		for (int i = 0; i < columns.size(); i++)
			paintTextInCell(graphics, columns.get(i).getAttributeKey(), treeTable.getHeaderBounds(i));
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
