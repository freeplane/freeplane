package org.freeplane.view.swing.map;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.freeplane.features.treetable.mindmapmode.TreeTableEditing;

class TreeTableCellEditor extends MouseAdapter {
	private final MapView mapView;

	TreeTableCellEditor(final MapView mapView) {
		this.mapView = mapView;
	}

	@Override
	public void mousePressed(final MouseEvent event) {
		if (event.isConsumed() || event.getButton() != MouseEvent.BUTTON1)
			return;
		for (final TreeTableView treeTable : TreeTableView.allIn(mapView)) {
			final TreeTableCell cell = treeTable.cellAt(event.getX(), event.getY());
			if (cell != null) {
				event.consume();
				TreeTableEditing.selectCellContent(cell.getRow(), cell.getAttributeKey());
				return;
			}
		}
	}
}
