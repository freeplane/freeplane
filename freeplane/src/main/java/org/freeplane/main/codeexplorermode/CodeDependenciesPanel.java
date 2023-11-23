package org.freeplane.main.codeexplorermode;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.textchanger.TranslatedElementFactory;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;

import com.tngtech.archunit.core.domain.Dependency;

class CodeDependenciesPanel extends JPanel implements INodeSelectionListener, IMapSelectionListener, IFreeplanePropertyListener{

    private enum SortOrder {
        SOURCE(Comparator.<Dependency, String>comparing(dep -> dep.getOriginClass().getName())),
        TARGET(Comparator.<Dependency, String>comparing(dep -> dep.getTargetClass().getName()));
        final Comparator<Dependency> comparator;

        private SortOrder(Comparator<Dependency> comparator) {
            this.comparator = comparator;
        }

    }

    private static final long serialVersionUID = 1L;
    private static final Icon filterIcon = ResourceController.getResourceController().getIcon("filterDependencyIncormation.icon");
    private final JTextField filterField;
    private final JTable dependencyViewer;
    private final JLabel countLabel;
    private SortOrder sortOrder;
    private List<Dependency> allDependencies;

    CodeDependenciesPanel() {
     // Create the top panel for sorting options
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        // Add components to the top panel
        topPanel.add(TranslatedElementFactory.createLabel("code.sort_by"));
        JRadioButton sourceButton = TranslatedElementFactory.createRadioButton("code.source");
        sourceButton.setSelected(true);
        sortOrder = SortOrder.SOURCE;
        sourceButton.addActionListener(e -> update(SortOrder.SOURCE));
        JRadioButton targetButton = TranslatedElementFactory.createRadioButton("code.target");
        targetButton.addActionListener(e -> update(SortOrder.TARGET));
        // Group the radio buttons
        ButtonGroup group = new ButtonGroup();
        group.add(sourceButton);
        group.add(targetButton);
        topPanel.add(sourceButton);
        topPanel.add(targetButton);

        topPanel.add(new JLabel(filterIcon));
        filterField = new JTextField(40);
        filterField.addActionListener(e -> filterAndSetDescriptions());
        topPanel.add(filterField);
        countLabel = new JLabel();
        topPanel.add(countLabel);

        dependencyViewer = new JTable();
        dependencyViewer.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dependencyViewer.getTableHeader().setVisible(false);
        dependencyViewer.getTableHeader().setPreferredSize(new Dimension(-1, 0));
        JTextField cellEditor = new JTextField();
        cellEditor.setEditable(false);
        dependencyViewer.setDefaultEditor(Object.class, new DefaultCellEditor(cellEditor));

        JScrollPane scrollPane = new JScrollPane(dependencyViewer);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void update(SortOrder sortOrder) {
        if(this.sortOrder != sortOrder) {
            this.sortOrder = sortOrder;
            sortAndFilter();
        }
    }

    @Override
    public void afterMapChange(MapModel oldMap, MapModel newMap) {
        if(newMap instanceof CodeMapModel)
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
        this.allDependencies = selection == null
                ? Collections.emptyList() :
                    new DependencySelection(selection).getSelectedDependencies();
        sortAndFilter();
    }

    private CodeLinkController linkController() {
        return (CodeLinkController)Controller.getCurrentModeController()
                .getExtension(LinkController.class);
    }

    private void sortAndFilter() {
        allDependencies.sort(sortOrder.comparator);
        filterAndSetDescriptions();

    }

    private void filterAndSetDescriptions() {
        Stream<String> allDescripions = allDependencies.stream()
                .map(Dependency::getDescription);
        String[] filteredWords = filterField.getText().trim().split("\\W+");
        Stream<String> filteredDescriptions  = filteredWords.length >= 1 && ! filteredWords[0].isEmpty()
                ? allDescripions.filter(t -> filter(t, filteredWords)) : allDescripions;
        DefaultTableModel newTableModel = new DefaultTableModel(new Object[]{"Dependencies"}, 0);

        filteredDescriptions.forEach(description -> newTableModel.addRow(new Object[]{description}));

        // Step 4: Manage Selections
        int selectedRow = dependencyViewer.getSelectedRow();
        String selectedValue = selectedRow >= 0 ? (String) dependencyViewer.getValueAt(selectedRow, 0) : null;

        // Step 3: Set the Model for JTable
        dependencyViewer.setModel(newTableModel);

        int newSelectedIndex = -1;
        if(selectedValue != null)
            for (int i = 0; i < newTableModel.getRowCount(); i++) {
                if (newTableModel.getValueAt(i, 0).equals(selectedValue)) {
                    newSelectedIndex = i;
                    break;
                }
            }

        // Step 5: Ensure Row Visibility
        if (newSelectedIndex != -1) {
            dependencyViewer.setRowSelectionInterval(newSelectedIndex, newSelectedIndex);
            dependencyViewer.scrollRectToVisible(new Rectangle(dependencyViewer.getCellRect(newSelectedIndex, 0, true)));
        }        countLabel.setText("( " + newTableModel.getRowCount() + " / " + allDependencies.size() + " )");
    }

    private boolean filter(String description, String[] filteredWords) {
        return Stream.of(filteredWords).allMatch(description::contains);
    }

    @Override
    public void propertyChanged(String propertyName, String newValue, String oldValue) {
        if(propertyName.equals("code_showOutsideDependencies")) {
            Controller controller = Controller.getCurrentController();
            IMapSelection selection = controller.getSelection();
            if (selection.getMap() instanceof CodeMapModel) {
                update(selection);
                controller.getMapViewManager().getMapViewComponent().repaint();
            }
        }
    }
}