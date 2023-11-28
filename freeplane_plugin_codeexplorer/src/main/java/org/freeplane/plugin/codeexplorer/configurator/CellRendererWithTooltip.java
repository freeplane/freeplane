/*
 * Created on 28 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.configurator;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
class CellRendererWithTooltip extends DefaultTableCellRenderer {
    @Override
    protected void setValue(Object value) {
        setText((value == null) ? "" : value.toString());
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (getPreferredSize().width > table.getColumnModel().getColumn(column).getWidth()) {
            setToolTipText(getText());
        } else {
            setToolTipText(null);
        }
        return this;
    }
}