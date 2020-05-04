package org.freeplane.features.fpsearch;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.EventObject;

class ShowPreferenceItemAction extends AbstractAction {
    private PreferencesItem preferencesItem;

    ShowPreferenceItemAction(PreferencesItem preferencesItem) {
        super("Show");
        this.preferencesItem = preferencesItem;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        System.out.format("Showing preferences item: %s\n", preferencesItem);
    }
}

public class PreferencesItemsResultTable extends JTable implements TableCellEditor, TableCellRenderer {

    private DefaultTableModel tableModel = new DefaultTableModel(0, 3) {
        public Class getColumnClass(int columnIndex)
        {
            if (columnIndex == 2)
                return PreferencesItem.class;
            else
                return String.class;
        }

        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return columnIndex == 2;
        }
    };
    private java.util.List<JButton> buttons = new ArrayList<>();

    PreferencesItemsResultTable()
    {
        setModel(tableModel);

        TableColumn keyColumn = getColumnModel().getColumn(0);
        keyColumn.setHeaderValue("Key");
        TableColumn textColumn = getColumnModel().getColumn(1);
        textColumn.setHeaderValue("Value");
        TableColumn showColumn = getColumnModel().getColumn(2);
        showColumn.setHeaderValue("Link");

        showColumn.setCellRenderer(this);
        showColumn.setCellEditor(this);

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                int column = columnAtPoint(e.getPoint());
                if (column == 2)
                {
                    JButton button = buttons.get(row);
                    button.doClick();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }

    private void allocateButton(int row)
    {
        while (buttons.size() <= row)
        {
            buttons.add(null);
        }
    }

    void addPreferencesItem(PreferencesItem preferencesItem)
    {
        tableModel.addRow(new Object[] { preferencesItem.key, preferencesItem.text, preferencesItem });
    }

    public void clear()
    {
        buttons.clear();
        tableModel.setRowCount(0);
        revalidate();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (column == 2)
        {
            allocateButton(row);
            if (buttons.get(row) == null)
            {
                buttons.set(row, new JButton(new ShowPreferenceItemAction((PreferencesItem)value)));
            }
            return buttons.get(row);
        }
        else
        {
            return new JLabel((String)value);
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (column == 2)
        {
            allocateButton(row);
            if (buttons.get(row) == null)
            {
                JButton button = new JButton(new ShowPreferenceItemAction((PreferencesItem) value));
                buttons.set(row, button);
            }
            return buttons.get(row);
        }
        else
        {
            return new JLabel((String)value);
        }
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return false;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return false;
    }

    @Override
    public boolean stopCellEditing() {
        return false;
    }

    @Override
    public void cancelCellEditing() {

    }

    @Override
    public void addCellEditorListener(CellEditorListener l) {

    }

    @Override
    public void removeCellEditorListener(CellEditorListener l) {

    }

}
