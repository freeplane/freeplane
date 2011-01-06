package org.freeplane.view.swing.map.attribute;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JViewport;
import javax.swing.table.JTableHeader;

class AttributeTableHeaderMouseListener extends MouseAdapter {
	@Override
	public void mouseReleased(final MouseEvent e) {
		final JTableHeader header = (JTableHeader) e.getSource();
		final AttributeTable table = (AttributeTable) header.getTable();
		final float zoom = table.getZoom();
		final Dimension preferredScrollableViewportSize = table.getPreferredScrollableViewportSize();
		final JViewport port = (JViewport) table.getParent();
		final Dimension extentSize = port.getExtentSize();
		if (preferredScrollableViewportSize.width != extentSize.width) {
			final AttributeTableModelDecoratorAdapter model = (AttributeTableModelDecoratorAdapter) table
			    .getModel();
			for (int col = 0; col < table.getColumnCount(); col++) {
				final int modelColumnWidth = model.getColumnWidth(col);
				final int currentColumnWidth = (int) (table.getColumnModel().getColumn(col).getWidth() / zoom);
				if (modelColumnWidth != currentColumnWidth) {
					model.setColumnWidth(col, currentColumnWidth);
				}
			}
		}
	}
}