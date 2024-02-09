/*
 * Created on 8 Feb 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.configurator;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.JTable;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.View;

import org.freeplane.core.util.HtmlUtils;

@SuppressWarnings("serial")
class WrappingTableCellRenderer extends DefaultTableCellRenderer{
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        final String html = HtmlUtils.plainToHTML(String.valueOf(value));
        super.getTableCellRendererComponent(table, html, isSelected, hasFocus, row, column);
        View view = (View) getClientProperty(BasicHTML.propertyKey);
        final Insets insets = getInsets();
        int columnWidth = table.getTableHeader().getColumnModel().getColumn(column).getWidth();
        view.setSize(columnWidth - insets.left - insets.right, 0.0f);
        int height = (int) view.getPreferredSpan(View.Y_AXIS) + insets.bottom + insets.top;
        if (table.getRowHeight(row) != height) {
            table.setRowHeight(row, height);
        }
        return this;
    }

    @Override
    public void paint(Graphics g) {
        View view = (View) getClientProperty(BasicHTML.propertyKey);
        final Insets insets = getInsets();
        view.setSize(getWidth() - insets.left - insets.right, getHeight() - insets.bottom - insets.top);
        super.paint(g);
    }


}