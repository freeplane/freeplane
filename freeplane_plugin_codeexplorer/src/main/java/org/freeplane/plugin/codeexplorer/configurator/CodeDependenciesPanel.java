package org.freeplane.plugin.codeexplorer.configurator;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.RowSorterEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.codeexplorer.dependencies.CodeDependency;
import org.freeplane.plugin.codeexplorer.map.DependencySelection;

class CodeDependenciesPanel extends JPanel implements INodeSelectionListener, IMapSelectionListener, IFreeplanePropertyListener{

    private static final String[] COLUMN_NAMES = new String[]{"Verdict", "Origin", "Target","Dependency"};

    private static final long serialVersionUID = 1L;
    private static final Icon filterIcon = ResourceController.getResourceController().getIcon("filterDependencyIncormation.icon");
    private final JTextField filterField;
    private final JTable dependencyViewer;
    private final JLabel countLabel;
    private List<CodeDependency> allDependencies;

    private class DependenciesWrapper extends AbstractTableModel {
        private static final long serialVersionUID = 1L;

        @Override
        public int getRowCount() {
            return allDependencies.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            CodeDependency row = allDependencies.get(rowIndex);
            switch (columnIndex) {
                case 0: return row.describeVerdict();
                case 1: return row.getOriginClass().getName();
                case 2: return row.getTargetClass().getName();
                case 3: return row.getDescription();
                default: return null;
            }
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex > 0;
        }
    }

    CodeDependenciesPanel() {
     // Create the top panel for sorting options
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        // Add components to the top panel

        topPanel.add(new JLabel(filterIcon));
        countLabel = new JLabel();
        topPanel.add(countLabel);
        filterField = new JTextField(100);
        filterField.addActionListener(e -> updateDependencyFilter());
        topPanel.add(filterField);

        dependencyViewer = new JTable();
        allDependencies = Collections.emptyList();
        DependenciesWrapper dataModel = new DependenciesWrapper();
        dependencyViewer.setModel(dataModel);
        CellRendererWithTooltip cellRenderer = new CellRendererWithTooltip();

        TableColumnModel columnModel = dependencyViewer.getColumnModel();
        updateColumn(columnModel, 0, 200, cellRenderer);
        updateColumn(columnModel, 1, 400, cellRenderer);
        updateColumn(columnModel, 2, 400, cellRenderer);
        updateColumn(columnModel, 3, 1000, cellRenderer);
        dependencyViewer.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dependencyViewer.setCellSelectionEnabled(true);

        TableRowSorter<DependenciesWrapper> sorter = new TableRowSorter<>(dataModel);

        sorter.addRowSorterListener(e -> {
            if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
                SwingUtilities.invokeLater(this::scrollSelectedToVisible);
            }
        });

        dependencyViewer.setRowSorter(sorter);

        JTextField cellEditor = new JTextField();
        cellEditor.setEditable(false);
        dependencyViewer.setDefaultEditor(Object.class, new DefaultCellEditor(cellEditor));

        JScrollPane scrollPane = new JScrollPane(dependencyViewer);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }


    private void updateDependencyFilter() {
        String[] filteredWords = filterField.getText().toLowerCase().trim().split("[^\\w:.$]+");
        @SuppressWarnings("unchecked")
        TableRowSorter<DependenciesWrapper> rowSorter = (TableRowSorter<DependenciesWrapper>)dependencyViewer.getRowSorter();
        if(filteredWords.length == 1 && filteredWords[0].isEmpty())
            rowSorter.setRowFilter(null);
        else {
            RowFilter<DependenciesWrapper, Integer> dependencyFilter = new RowFilter<DependenciesWrapper, Integer>() {
                Predicate<CodeDependency> combinedFilter = Stream.of(filteredWords)
                        .map(this::createPredicateFromString)
                        .reduce(x -> true, Predicate::and);
                private Predicate<CodeDependency> createPredicateFromString(String searchedString) {
                    if (searchedString.startsWith("origin:")) {
                        String value = searchedString.substring("origin:".length());
                        return dependency -> dependency.getOriginClass().getName().toLowerCase().contains(value);
                    } else if (searchedString.startsWith("target:")) {
                        String value = searchedString.substring("target:".length());
                        return dependency -> dependency.getTargetClass().getName().toLowerCase().contains(value);
                    } else if (searchedString.startsWith("verdict:")) {
                        String value = searchedString.substring("verdict:".length());
                        return dependency -> dependency.describeVerdict().contains(value);
                    } else if (searchedString.startsWith("dependency:")) {
                        String value = searchedString.substring("dependency:".length());
                        return dependency -> dependency.getDescription().toLowerCase().contains(value);
                    } else {
                        return dependency -> dependency.getDependency().getDescription().toLowerCase().contains(searchedString) || dependency.describeVerdict().contains(searchedString);
                    }
                }

                @Override
                public boolean include(RowFilter.Entry<? extends DependenciesWrapper, ? extends Integer> entry) {
                    CodeDependency row = allDependencies.get(entry.getIdentifier());
                    return combinedFilter.test(row);
                }
            };

            rowSorter.setRowFilter(dependencyFilter);
        }
        scrollSelectedToVisible();
        countLabel.setText("( " + rowSorter.getViewRowCount() + " / " + rowSorter.getModelRowCount() + " )");
    }
    private void updateColumn(TableColumnModel columns, int index, int columnWidth, TableCellRenderer cellRenderer) {
        int scaledWidth = (int) (columnWidth*UITools.FONT_SCALE_FACTOR);
        TableColumn columnModel = columns.getColumn(index);
        columnModel.setWidth(scaledWidth);
        columnModel.setPreferredWidth(scaledWidth);
        columnModel.setCellRenderer(cellRenderer);
    }

    @Override
    public void afterMapChange(MapModel oldMap, MapModel newMap) {
        update();
    }

    void update() {
        Controller controller = Controller.getCurrentController();
        update(controller.getSelection());
    }

    @Override
    public void onSelectionSetChange(IMapSelection selection) {
        update(selection);
    }


    private void update(IMapSelection selection) {
        CodeDependency selectedValue = getSelectedDependency();
        int selectedColumn = dependencyViewer.getSelectedColumn();
        this.allDependencies = selection == null
                ? Collections.emptyList() :
                    selectedDependencies(new DependencySelection(selection));
        ((DependenciesWrapper)dependencyViewer.getModel()).fireTableDataChanged();
        updateRowCountLabel();
        if(selectedValue != null) {
            int newSelectedDataIndex = allDependencies.indexOf(selectedValue);
            if(newSelectedDataIndex >= 0) {
                int newSelectedRow = dependencyViewer.convertRowIndexToView(newSelectedDataIndex);
                if(newSelectedRow != -1) {
                    dependencyViewer.setRowSelectionInterval(newSelectedRow, newSelectedRow);
                    dependencyViewer.setColumnSelectionInterval(selectedColumn, selectedColumn);
                    SwingUtilities.invokeLater(this::scrollSelectedToVisible);
                }
            }
        }

    }

    private List<CodeDependency> selectedDependencies(DependencySelection dependencySelection) {
        return dependencySelection.getSelectedDependencies().map(dependencySelection.getMap()::toCodeDependency)
        .collect(Collectors.toCollection(ArrayList::new));
    }

    private CodeDependency getSelectedDependency() {
        int selectedRow = dependencyViewer.getSelectedRow();
        int selectedDataIndex = selectedRow < 0 ? -1 : dependencyViewer.convertRowIndexToModel(selectedRow);
        CodeDependency selectedValue = selectedDataIndex < 0 ? null : allDependencies.get(selectedDataIndex);
        return selectedValue;
    }

    private void updateRowCountLabel() {
        countLabel.setText("( " + dependencyViewer.getRowCount() + " / " + allDependencies.size() + " )");
    }

    private void scrollSelectedToVisible() {
        int selectedRowOnView = dependencyViewer.getSelectedRow();
        if (selectedRowOnView != -1) {
            dependencyViewer.scrollRectToVisible(new Rectangle(dependencyViewer.getCellRect(selectedRowOnView, 0, true)));
        }
    }

    @Override
    public void propertyChanged(String propertyName, String newValue, String oldValue) {
        if(propertyName.equals("code_showOutsideDependencies")) {
            Controller controller = Controller.getCurrentController();
            IMapSelection selection = controller.getSelection();
            update(selection);
            controller.getMapViewManager().getMapViewComponent().repaint();
        }
    }

    void addDependencySelectionCallback(Consumer<CodeDependency> listener) {
        dependencyViewer.getSelectionModel().addListSelectionListener(
                e -> {
                    if(!e.getValueIsAdjusting())
                        listener.accept(getSelectedDependency());
                });
    }
}