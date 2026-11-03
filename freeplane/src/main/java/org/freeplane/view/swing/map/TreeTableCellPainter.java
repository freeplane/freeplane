package org.freeplane.view.swing.map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.List;

import org.freeplane.features.treetable.TreeTableGrid;

class TreeTableCellPainter {
	private final MapView mapView;

	TreeTableCellPainter(final MapView mapView) {
		this.mapView = mapView;
	}

	void paintTreeTables(final Graphics2D graphics) {
		for (final TreeTableView treeTable : TreeTableView.allIn(mapView))
			paintTreeTable(graphics, treeTable);
	}

	private void paintTreeTable(final Graphics2D graphics, final TreeTableView treeTable) {
		paintGrid(graphics, treeTable);
	}

	private void paintGrid(final Graphics2D graphics, final TreeTableView treeTable) {
		final TreeTableGrid grid = treeTable.getGrid();
		final Stroke previousStroke = graphics.getStroke();
		final Color previousColor = graphics.getColor();
		final List<NodeView> rows = treeTable.getRows();
		for (int i = 0; i < grid.getRowCount(); i++) {
			useLineStyle(graphics, TreeTableLineStyle.ofBorder(rows.get(i)));
			drawHorizontalLine(graphics, grid, grid.getRowBottomY(i));
		}
		for (final TreeTableView.HeaderLine line : treeTable.getHeaderLines()) {
			useLineStyle(graphics, TreeTableLineStyle.ofBorder(line.view));
			graphics.drawLine(line.left, line.y, line.right, line.y);
		}
		useLineStyle(graphics, TreeTableLineStyle.ofBorder(treeTable.getTreeTableRootView()));
		graphics.drawRect(grid.getLeft(), grid.getTop(), grid.getRight() - grid.getLeft(),
			grid.getBottom() - grid.getTop());
		drawHorizontalLine(graphics, grid, grid.getHeaderBottom());
		for (int i = 0; i < grid.getColumnSeparatorCount(); i++)
			graphics.drawLine(grid.getColumnSeparatorX(i), grid.getColumnSeparatorTop(i), grid.getColumnSeparatorX(i),
				grid.getBottom());
		graphics.setStroke(previousStroke);
		graphics.setColor(previousColor);
	}

	private static void drawHorizontalLine(final Graphics2D graphics, final TreeTableGrid grid, final int y) {
		graphics.drawLine(grid.getLeft(), y, grid.getRight(), y);
	}

	private static void useLineStyle(final Graphics2D graphics, final TreeTableLineStyle lineStyle) {
		graphics.setColor(lineStyle.getColor());
		graphics.setStroke(lineStyle.stroke());
	}
}
