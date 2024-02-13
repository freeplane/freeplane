package org.freeplane.plugin.codeexplorer.configurator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JComponent;
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
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.codeexplorer.dependencies.CodeDependency;
import org.freeplane.plugin.codeexplorer.map.ClassNode;
import org.freeplane.plugin.codeexplorer.map.CodeMap;
import org.freeplane.plugin.codeexplorer.map.CodeNode;
import org.freeplane.plugin.codeexplorer.map.DependencySelection;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;

class CodeDependenciesPanel extends JPanel implements INodeSelectionListener, IMapSelectionListener, IFreeplanePropertyListener, IMapChangeListener{

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
                case 1: return ClassNode.nodeText(row.getOriginClass());
                case 2: return ClassNode.nodeText(row.getTargetClass());
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

        dependencyViewer = new JTable() {

            private static final long serialVersionUID = 1L;

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                JComponent component = (JComponent) super.prepareRenderer(renderer, row, column);
                int modelColumn = convertColumnIndexToModel(column);
                if(modelColumn == 1 || modelColumn == 2) {
                    CodeDependency codeDependency = allDependencies.get(convertRowIndexToModel(row));
                    JavaClass javaClass = modelColumn == 1 ? codeDependency.getOriginClass() : codeDependency.getTargetClass();
                    component.setToolTipText(toDisplayedFullName(javaClass));
                }
                return component;
            }

        };
        allDependencies = Collections.emptyList();
        DependenciesWrapper dataModel = new DependenciesWrapper();
        dependencyViewer.setModel(dataModel);
        CellRendererWithTooltip cellRenderer = new CellRendererWithTooltip();

        TableColumnModel columnModel = dependencyViewer.getColumnModel();
        updateColumn(columnModel, 0, 200, cellRenderer);
        updateColumn(columnModel, 1, 200, cellRenderer);
        updateColumn(columnModel, 2, 200, cellRenderer);
        updateColumn(columnModel, 3, 1200, cellRenderer);

        dependencyViewer.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        dependencyViewer.getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
        String[] filteredWords = filterField.getText().trim().split("[^\\w:.$]+");
        @SuppressWarnings("unchecked")
        TableRowSorter<DependenciesWrapper> rowSorter = (TableRowSorter<DependenciesWrapper>)dependencyViewer.getRowSorter();
        if(filteredWords.length == 1 && filteredWords[0].isEmpty())
            rowSorter.setRowFilter(null);
        else {
            RowFilter<DependenciesWrapper, Integer> dependencyFilter = new RowFilter<DependenciesWrapper, Integer>() {
                Predicate<String[]> combinedFilter = Stream.of(filteredWords)
                        .map(this::createPredicateFromString)
                        .reduce(x -> true, Predicate::and);
                private Predicate<String[]> createPredicateFromString(String searchedString) {
                    if (searchedString.startsWith("origin:")) {
                        String value = searchedString.substring("origin:".length());
                        return row -> row[1].contains(value);
                    } else if (searchedString.startsWith("target:")) {
                        String value = searchedString.substring("target:".length());
                        return row -> row[2].contains(value);
                    } else if (searchedString.startsWith("verdict:")) {
                        String value = searchedString.substring("verdict:".length());
                        return row -> row[0].contains(value);
                    } else if (searchedString.startsWith("dependency:")) {
                        String value = searchedString.substring("dependency:".length());
                        return row -> row[3].contains(value);
                    } else {
                        return row -> Stream.of(row).anyMatch(s-> s.contains(searchedString));
                    }
                }

                @Override
                public boolean include(RowFilter.Entry<? extends DependenciesWrapper, ? extends Integer> entry) {
                    TableModel tableData = dependencyViewer.getModel();
                    String[] row = IntStream.range(0, 4)
                            .mapToObj(column -> tableData.getValueAt(entry.getIdentifier(), column).toString())
                            .toArray(String[]::new);
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

    @Override
    public void mapChanged(MapChangeEvent event) {
        if(event.getProperty().equals(Filter.class))
            SwingUtilities.invokeLater(this::update);
    }


    private void update(IMapSelection selection) {
        Set<CodeDependency> selectedDependencies = getSelectedDependencies().collect(Collectors.toSet());
        int selectedColumn = dependencyViewer.getSelectedColumn();
        this.allDependencies = selection == null || ! (selection.getMap() instanceof CodeMap)
                ? Collections.emptyList() :
                    selectedDependencies(new DependencySelection(selection));
        ((DependenciesWrapper)dependencyViewer.getModel()).fireTableDataChanged();
        updateRowCountLabel();
        if(! selectedDependencies.isEmpty()) {
            IntStream.range(0, allDependencies.size())
            .filter(i -> selectedDependencies.contains(allDependencies.get(i)))
            .map(dependencyViewer::convertRowIndexToView)
            .forEach(row -> dependencyViewer.addRowSelectionInterval(row, row));
            if(dependencyViewer.getSelectedRow() != -1) {
                dependencyViewer.setColumnSelectionInterval(selectedColumn, selectedColumn);
                SwingUtilities.invokeLater(this::scrollSelectedToVisible);
            }
        }
    }

    private List<CodeDependency> selectedDependencies(DependencySelection dependencySelection) {
        return dependencySelection.getSelectedDependencies().map(dependencySelection.getMap()::toCodeDependency)
        .collect(Collectors.toCollection(ArrayList::new));
    }

    private Stream<CodeDependency> getSelectedDependencies() {
        return IntStream.of(dependencyViewer.getSelectedRows())
        .map(dependencyViewer::convertRowIndexToModel)
        .mapToObj(allDependencies::get);
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

    void addDependencySelectionCallback(Consumer<List<Dependency> > listener) {
        dependencyViewer.getSelectionModel().addListSelectionListener(
                e -> {
                    if(!e.getValueIsAdjusting())
                        listener.accept(getSelectedDependencyList());
                });
        dependencyViewer.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                if(! e.isTemporary())
                    listener.accept(getSelectedDependencyList());
            }

        });
    }


    public List<Dependency> getSelectedDependencyList() {
        return getSelectedDependencies().map(CodeDependency::getDependency).collect(Collectors.toList());
    }


    private String toDisplayedFullName(JavaClass originClass) {
        return CodeNode.findEnclosingNamedClass(originClass).getName().replace('$', '.');
    }
}