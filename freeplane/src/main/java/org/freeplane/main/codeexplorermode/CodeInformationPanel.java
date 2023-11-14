package org.freeplane.main.codeexplorermode;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.textchanger.TranslatedElementFactory;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

import com.tngtech.archunit.core.domain.Dependency;

class CodeInformationPanel extends JPanel implements INodeSelectionListener, IMapSelectionListener{

    enum SortOrder {
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
    private final JList<String> depencencyViewer;
    private final JLabel countLabel;
    private SortOrder sortOrder;
    private ArrayList<Dependency> allDependencies;

    CodeInformationPanel() {
     // Create the top panel for sorting options
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        // Add components to the top panel
        topPanel.add(new JLabel("Sort by:"));
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

        depencencyViewer = new JList<>();
        JScrollPane scrollPane = new JScrollPane(depencencyViewer);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void update(SortOrder sortOrder) {
        if(this.sortOrder != sortOrder) {
            this.sortOrder = sortOrder;
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


    void update(IMapSelection selection) {
        Set<NodeModel> nodes = selection.getSelection();
        if(nodes.size() == 1) {
            CodeNodeModel selectedNode = (CodeNodeModel) selection.getSelected();
            Set<Dependency> outgoingDependencies = selectedNode.getOutgoingDependencies(selection);
            Set<Dependency> incomingDependencies = selectedNode.getIncomingDependencies(selection);
            allDependencies = new ArrayList<Dependency>(outgoingDependencies.size() + incomingDependencies.size());
            allDependencies.addAll(outgoingDependencies);
            allDependencies.addAll(incomingDependencies);
        }
        else {
            allDependencies = nodes.stream()
                .map(node -> ((CodeNodeModel)node).getOutgoingDependencies(selection))
                .flatMap(Set::stream)
                .collect(Collectors.toCollection(ArrayList::new));
        }
        sortAndFilter();
    }

    void sortAndFilter() {
        allDependencies.sort(sortOrder.comparator);
        filterAndSetDescriptions();

    }

    private void filterAndSetDescriptions() {
        Stream<String> allDescripions = allDependencies.stream()
                .map(Dependency::getDescription);
        String[] filteredWords = filterField.getText().trim().split("\\W+");
        Stream<String> filteredDescriptions  = filteredWords.length >= 1 && ! filteredWords[0].isEmpty()
                ? allDescripions.filter(t -> filter(t, filteredWords)) : allDescripions;
        DefaultListModel<String> newDataModel = new DefaultListModel<>();
        filteredDescriptions.forEach(newDataModel::addElement);
        String selectedValue = depencencyViewer.getSelectedValue();
        int newSelectedIndex = newDataModel.indexOf(selectedValue);
        depencencyViewer.setModel(newDataModel);
        depencencyViewer.setSelectedIndex(newSelectedIndex >= 0 ? newSelectedIndex : 0);
        countLabel.setText("( " + newDataModel.getSize() + " / " + allDependencies.size() + " )");
    }

    private boolean filter(String description, String[] filteredWords) {
        return Stream.of(filteredWords).allMatch(description::contains);
    }
}