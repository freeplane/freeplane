/*
 * Created on 10 May 2024
 *
 * author dimitry
 */
package org.freeplane.view.swing.features.time.mindmapmode.nodelist;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class TableColumnVisibilityChanger {

    private final TableColumnModel columnModel;
    private int[] originalColumnWidths;

    public TableColumnVisibilityChanger(TableColumnModel columnModel) {
        this.columnModel = columnModel;
    }

    public JMenu createMenu(String menuTitle) {
        JMenu menu = new JMenu(menuTitle);
        addMenuItems(menu);
        return menu;
    }

    public void addMenuItems(JMenu viewMenu) {
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn column = columnModel.getColumn(i);
            JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(column.getHeaderValue().toString(),
                    isColumnVisible(i));
            int columnIndex = i;
            menuItem.addActionListener(e -> {
                toggleColumnVisibility(columnIndex);
                menuItem.setSelected(column.getPreferredWidth() != 0);
            });
            viewMenu.add(menuItem);
        }
    }

    private void toggleColumnVisibility(int columnIndex) {
        setColumnVisible(columnIndex, ! isColumnVisible(columnIndex));
    }

    public void setColumnVisible(int columnIndex, final boolean becomesVisible) {
        if(becomesVisible == isColumnVisible(columnIndex))
            return;
        TableColumn column = columnModel.getColumn(columnIndex);
        initializeColumnWidths();

        if (becomesVisible) {
            showColumn(columnIndex, column);
            originalColumnWidths[columnIndex] = 0;
        } else {
            originalColumnWidths[columnIndex] = column.getPreferredWidth();
            hideColumn(column);
        }
    }

    private void showColumn(int columnIndex, TableColumn column) {
        column.setMinWidth(15);
        column.setMaxWidth(Integer.MAX_VALUE);
        column.setPreferredWidth(originalColumnWidths[columnIndex]);
    }

    private void hideColumn(TableColumn column) {
        column.setMinWidth(0);
        column.setMaxWidth(0);
        column.setPreferredWidth(0);
    }

    private void initializeColumnWidths() {
        if(originalColumnWidths == null)
            originalColumnWidths = new int[columnModel.getColumnCount()];
    }

    public boolean isColumnVisible(int columnIndex) {
        return columnModel.getColumn(columnIndex).getPreferredWidth() > 0;
    }

    public String getState() {
        initializeColumnWidths();
        return IntStream.of(originalColumnWidths).mapToObj(Integer::toString).collect(Collectors.joining(","));
    }

    public void applyState(String state) {
        if(state == null || state.isEmpty())
            return;
        final String[] split = state.split(",");
        if(split == null || split.length != columnModel.getColumnCount())
            return;
        originalColumnWidths = Stream.of(split).mapToInt(Integer::parseInt).toArray();
        for(int i = 0; i < originalColumnWidths.length; i++) {
            if(originalColumnWidths[i] != 0)
                hideColumn(columnModel.getColumn(i));
        }
    }
}
