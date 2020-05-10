package org.freeplane.features.fpsearch;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.OptionPanel;
import org.freeplane.core.resources.components.OptionPanelBuilder;
import org.freeplane.core.resources.components.ShowPreferencesAction;
import org.freeplane.features.mode.mindmapmode.MModeController;

class ShowPreferenceItemAction extends AbstractAction {
    private PreferencesItem preferencesItem;

    ShowPreferenceItemAction(PreferencesItem preferencesItem) {
        super("Show");
        this.preferencesItem = preferencesItem;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        System.out.format("Showing preferences item: %s\n", preferencesItem);
        showPrefsDialog();
    }

    private void showPrefsDialog()
    {
        OptionPanelBuilder optionPanelBuilder = new OptionPanelBuilder();
        final ResourceController resourceController = ResourceController.getResourceController();
        URL preferences = resourceController.getResource("/xml/preferences.xml");
        optionPanelBuilder.load(preferences);
        ShowPreferencesAction showPreferencesAction = MModeController.createShowPreferencesAction(optionPanelBuilder, this.preferencesItem);
        int uniqueId = new Long(System.currentTimeMillis()).intValue();
        showPreferencesAction.actionPerformed(
                new ActionEvent(this, uniqueId, OptionPanel.OPTION_PANEL_RESOURCE_PREFIX + preferencesItem.tab));
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
                    //JButton button = buttons.get(row);
                    //button.doClick();
                    PreferencesItem preferencesItem = (PreferencesItem)getModel().getValueAt(row, column);
                    assert(preferencesItem != null);
                    new ShowPreferenceItemAction(preferencesItem).actionPerformed(null);
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
                PreferencesItem preferencesItem = (PreferencesItem) value;
                assert(preferencesItem != null);
                buttons.set(row, new JButton(new ShowPreferenceItemAction(preferencesItem)));
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
                PreferencesItem preferencesItem = (PreferencesItem) value;
                assert(preferencesItem != null);
                JButton button = new JButton(new ShowPreferenceItemAction(preferencesItem));
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
