package org.freeplane.view.swing.map;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.treetable.TreeTableColumn;
import org.freeplane.features.treetable.TreeTableColumns;
import org.freeplane.features.treetable.mindmapmode.TreeTableEditing;

class TreeTableColumnResizer extends MouseAdapter {
	private static final int RESIZE_TOLERANCE = 3;
	private static final Cursor RESIZE_CURSOR = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);

	private final MapView mapView;

	private NodeModel resizedTableRoot;
	private TreeTableColumns columnsBeforeResize;
	private String resizedAttributeKey;
	private int resizedColumnX;
	private boolean showsResizeCursor;

	TreeTableColumnResizer(final MapView mapView) {
		this.mapView = mapView;
	}

	@Override
	public void mouseMoved(final MouseEvent event) {
		if (isResizing())
			return;
		final boolean nearResizeEdge = resizeEdgeAt(event.getX(), event.getY()) != null;
		if (nearResizeEdge != showsResizeCursor) {
			showsResizeCursor = nearResizeEdge;
			mapView.setCursor(nearResizeEdge ? RESIZE_CURSOR : Cursor.getDefaultCursor());
		}
	}

	@Override
	public void mousePressed(final MouseEvent event) {
		if (event.getButton() != MouseEvent.BUTTON1)
			return;
		final ResizeEdge edge = resizeEdgeAt(event.getX(), event.getY());
		if (edge == null)
			return;
		resizedTableRoot = edge.treeTable.getTreeTableRoot();
		columnsBeforeResize = edge.treeTable.getColumns();
		resizedAttributeKey = columnsBeforeResize.get(edge.columnIndex).getAttributeKey();
		resizedColumnX = edge.treeTable.getColumnX(edge.columnIndex);
		event.consume();
	}

	@Override
	public void mouseDragged(final MouseEvent event) {
		if (!isResizing())
			return;
		mapView.setCursor(RESIZE_CURSOR);
		TreeTableEditing.previewColumnWidth(resizedTableRoot, columnsBeforeResize, resizedAttributeKey,
			widthAt(event.getX()));
		mapView.repaint();
		event.consume();
	}

	@Override
	public void mouseReleased(final MouseEvent event) {
		if (!isResizing())
			return;
		TreeTableEditing.resizeColumn(resizedTableRoot, columnsBeforeResize, resizedAttributeKey,
			widthAt(event.getX()));
		resizedTableRoot = null;
		columnsBeforeResize = null;
		resizedAttributeKey = null;
		showsResizeCursor = false;
		mapView.repaint();
	}

	private boolean isResizing() {
		return resizedTableRoot != null;
	}

	private Quantity<LengthUnit> widthAt(final int x) {
		return TreeTableColumn.widthFromZoomedPixels(x - resizedColumnX, mapView.getZoom());
	}

	private ResizeEdge resizeEdgeAt(final int x, final int y) {
		for (final TreeTableView treeTable : TreeTableView.allIn(mapView)) {
			final int columnIndex = treeTable.columnResizeEdgeAt(x, y, RESIZE_TOLERANCE);
			if (columnIndex >= 0)
				return new ResizeEdge(treeTable, columnIndex);
		}
		return null;
	}

	private static class ResizeEdge {
		private final TreeTableView treeTable;
		private final int columnIndex;

		private ResizeEdge(final TreeTableView treeTable, final int columnIndex) {
			this.treeTable = treeTable;
			this.columnIndex = columnIndex;
		}
	}
}
