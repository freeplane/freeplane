/*
 * Created on 8 Feb 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.configurator;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.freeplane.core.util.HtmlUtils;

@SuppressWarnings("serial")
class WrappingTableCellRenderer extends DefaultTableCellRenderer{
    static void adjustRowHeights(JTable table) {
        for (int row = 0; row < table.getRowCount(); row++) {
            adjustRowHeight(table, row);
        }
    }

    static void adjustRowHeight(JTable table, int row) {
        int rowHeight = 0;
        for (int column = 0; column < table.getColumnCount(); column++) {
            Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
            final Dimension preferredSize = comp.getPreferredSize();
            rowHeight = Math.max(rowHeight, preferredSize.height);
        }
        // Set the row height, allowing it to increase or decrease as needed
        table.setRowHeight(row, rowHeight);
    }
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        int columnWidth = table.getTableHeader().getColumnModel().getColumn(column).getWidth();
        final String style = "style=\"width:" + columnWidth/2 + "px;\"";
        final String html = HtmlUtils.plainToHTML(String.valueOf(value))
                .replaceFirst("<body>", "<body " + style + ">");
        setSize(columnWidth, Short.MAX_VALUE);
        super.getTableCellRendererComponent(table, html, isSelected, hasFocus, row, column);
        adjustTextAreaDimensions(table, row, column, columnWidth);
        return this;
    }

    private void adjustTextAreaDimensions(JTable table, int row, int column, int columnWidth) {
        int preferredHeight = getPreferredSize().height;
        if (table.getRowHeight(row) != preferredHeight) {
            table.setRowHeight(row, preferredHeight);
        }
    }
}