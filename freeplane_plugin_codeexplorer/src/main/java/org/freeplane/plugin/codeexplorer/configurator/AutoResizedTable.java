/*
 * Created on 30 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.configurator;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

@SuppressWarnings("serial") class AutoResizedTable extends JTable {
    {
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }
    public AutoResizedTable(TableModel dm) {
        super(dm);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);
        resizeAndRepaint();
    }

    @Override
    public void doLayout() {
        for(int column = 0; column < getColumnCount(); column ++){
            int  width = getParent().getWidth();
            for (int row = 0; row < getRowCount(); row++) {
                TableCellRenderer renderer = getCellRenderer(row, column);
                Component comp = prepareRenderer(renderer, row, column);
                width = Math.max (comp.getPreferredSize().width, width);
            }
            TableColumn col = new TableColumn();
            col = getColumnModel().getColumn(column);
            col.setPreferredWidth(width);
        }
        super.doLayout();
    }
}