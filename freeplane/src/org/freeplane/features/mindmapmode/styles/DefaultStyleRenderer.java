package org.freeplane.features.mindmapmode.styles;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.styles.MapStyleModel;

public class DefaultStyleRenderer implements TableCellRenderer {
	private TableCellRenderer renderer = new DefaultTableCellRenderer();
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
	                                               int row, int column) {
		final MapStyleModel styles = ((ConditionalStyleTable)table).getStyles();
		final NodeModel styleNode = styles.getStyleNode(value);
		if (styleNode == null){
			value = MapStyleModel.DEFAULT_STYLE;
		}
		return renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}
}
