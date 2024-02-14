package org.freeplane.plugin.codeexplorer.configurator;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.core.ui.textchanger.TranslatedElementFactory;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.codeexplorer.archunit.ArchUnitServer;
import org.freeplane.plugin.codeexplorer.archunit.ArchitectureViolationsConfiguration;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.freeplane.extension.ArchitectureViolations;

class ArchitectureViolationsPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private DefaultTableModel ruleTableModel;
    private DefaultTableModel violationTableModel;
    private JTable ruleTable;
    private JTable violationTable;
    private final CodeProjectController codeProjectController;
    private final ArchUnitServer archUnitServer;
    private Map<String, Dependency> violations;


    ArchitectureViolationsPanel(CodeProjectController codeProjectController, ArchUnitServer archUnitServer, AFreeplaneAction enableServerAction) {
        this.codeProjectController = codeProjectController;
        this.archUnitServer = archUnitServer;
        this.violations = Collections.emptyMap();
        archUnitServer.setCallback(this::testResultAdded);
        createPanels(enableServerAction);
        updateRuleTable();
    }

    private void testResultAdded(ArchitectureViolations result) {
        addNewTestResult(result.violatedRuleDescription);
    }


    private void updateRuleTable() {
        ruleTableModel.setRowCount(0); // Clear existing data
        for (ArchitectureViolations rule : submittedTestResults()) {
            ruleTableModel.addRow(new Object[]{rule.violatedRuleDescription});
        }
    }


    private JComponent createRulePanel() {
        ruleTableModel = createEmptyTableModel();
        ruleTable = new JTable(ruleTableModel);
        final WrappingTableCellRenderer renderer = new WrappingTableCellRenderer();
        ruleTable.setDefaultRenderer(Object.class, renderer);
        ruleTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        JScrollPane ruleTableScrollPane = new JScrollPane(ruleTable);

        ruleTable.getSelectionModel().addListSelectionListener(e -> updateViolations());

        return ruleTableScrollPane;
     }

    @SuppressWarnings("serial")
    private JComponent createViolationsPane() {

        violationTableModel = createEmptyTableModel();
        violationTable = new AutoResizedTable(violationTableModel);
        violationTable.getTableHeader().setVisible(false);
        violationTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        violationTable.getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        final WrappingTableCellRenderer renderer = new WrappingTableCellRenderer();
        TableColumn violationsColumn = violationTable.getColumnModel().getColumn(0);
        violationsColumn.setCellRenderer(renderer);
        JScrollPane violationsTableScrollPane = new JScrollPane(violationTable);
        violationsTableScrollPane.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                violationTable.revalidate();
                violationTable.repaint();
            }

        });
        return violationsTableScrollPane;
    }

    @SuppressWarnings("serial")
    private DefaultTableModel createEmptyTableModel() {
        return new DefaultTableModel(new Object[]{""}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
               return false;
            }
        };
    }
    private void updateViolations() {
        violationTableModel.setRowCount(0); // Clear existing data
        ArchitectureViolations rule = getSelectedTestResult();
        if (rule !=  null) {
            for (String description : rule.violationDescriptions) {
                violationTableModel.addRow(new Object[]{description});
            }
        }
    }

    private void exploreSelectedTestResult() {
        final ArchitectureViolations selectedTestResult = getSelectedTestResult();
        final ArchitectureViolationsConfiguration configuration = new ArchitectureViolationsConfiguration(selectedTestResult);
        violations = configuration.violations();
        codeProjectController.exploreConfiguration(configuration);
    }

    ArchitectureViolations getSelectedTestResult() {
        if(ruleTable.getSelectedRowCount() != 1)
            return null;

        int selectedTestResultIndex = ruleTable.getSelectedRow();
        ArchitectureViolations selectedTestResult = getTestResult(selectedTestResultIndex);
        return selectedTestResult;
    }

    private ArchitectureViolations getTestResult(int selectedTestResultIndex) {
        final List<ArchitectureViolations> submittedTestResults = submittedTestResults();
        if(selectedTestResultIndex >= 0 && selectedTestResultIndex < submittedTestResults.size()) {
            return submittedTestResults.get(selectedTestResultIndex);
        } else
            return null;
    }


    private List<ArchitectureViolations> submittedTestResults() {
        return archUnitServer.getSubmittedTestResults();
    }

    private void addNewTestResult(String violatedRuleDescription) {
        ruleTableModel.addRow(new Object[]{violatedRuleDescription});
        final int row = ruleTable.getRowCount() - 1;
        if(row == 0)
            ruleTable.addRowSelectionInterval(row, row);
    }

    private void deleteSelectedTestResults() {
        final ListSelectionModel selectionModel = ruleTable.getSelectionModel();
        int minSelectionIndex = selectionModel.getMinSelectionIndex();
        if(minSelectionIndex == -1)
            return;
        int maxSelectionIndex = selectionModel.getMaxSelectionIndex();
        for(int row = maxSelectionIndex; row >= minSelectionIndex; row--) {
            ruleTableModel.removeRow(row);
            submittedTestResults().remove(row);
        }
        int rowCount = ruleTableModel.getRowCount();
        if(minSelectionIndex < rowCount)
            ruleTable.setRowSelectionInterval(minSelectionIndex, minSelectionIndex);
        else if (rowCount > 0)
            ruleTable.setRowSelectionInterval(rowCount-1, rowCount-1);
        updateViolations();
    }

    private void cancelAnalysis() {
        codeProjectController.cancelAnalysis();
    }

    private void createPanels(AFreeplaneAction enableServerAction) {
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        JLabel rulesLabel = new JLabel(TextUtils.getText("code.rules"));
        JComponent rulesPanel = createRulePanel();
        JComponent ruleTableToolbar = createRuleTableToolbar(enableServerAction);
        JLabel violationsLabel = new JLabel(TextUtils.getText("code.violations"));
        JComponent violationsPane = createViolationsPane();


        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1; // Span across all columns

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor=GridBagConstraints.CENTER;
        add(rulesLabel, gbc);

        gbc.gridy = 1;
        gbc.anchor=GridBagConstraints.LINE_START;
        add(ruleTableToolbar, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor=GridBagConstraints.CENTER;
        add(violationsLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 1;
        rulesPanel.setPreferredSize(new Dimension(1, 1));
        add(rulesPanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 2;
        violationsPane.setPreferredSize(new Dimension(1, 1));
        add(violationsPane, gbc);
    }

    private JComponent createRuleTableToolbar(AFreeplaneAction enableServerAction) {
        FreeplaneToolBar toolbar = new FreeplaneToolBar(SwingConstants.HORIZONTAL);
        AbstractButton enableServerButton = FreeplaneToolBar.createButton(enableServerAction);
        JButton deleteTestResultButton = TranslatedElementFactory.createButtonWithIcon("code.delete");
        deleteTestResultButton.addActionListener(e -> deleteSelectedTestResults());
        JButton exploreTestResultButton = TranslatedElementFactory.createButtonWithIcon("code.explore");
        exploreTestResultButton.addActionListener(e -> exploreSelectedTestResult());

        ruleTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    exploreSelectedTestResult();
                }
            }
        });

        JButton cancelButton = TranslatedElementFactory.createButtonWithIcon("code.cancel");
        cancelButton.addActionListener(e -> cancelAnalysis());

        JComponent panelButtons[] = {enableServerButton, deleteTestResultButton, exploreTestResultButton, cancelButton};
        Stream.of(panelButtons).forEach(button -> {
            toolbar.add(button);
        });

        JButton enablingButtons[] = {exploreTestResultButton};

        Stream.of(enablingButtons).forEach(button -> {
            button.setEnabled(false);
        });

        Runnable enableButtons = () -> {
            final int selectedRowCount = ruleTable.getSelectedRowCount();
            deleteTestResultButton.setEnabled(selectedRowCount >= 1);
            boolean enable = selectedRowCount == 1;
            Stream.of(enablingButtons).forEach(button -> button.setEnabled(enable));
        };

        ruleTable.getSelectionModel().addListSelectionListener(l -> enableButtons.run());

        return toolbar;
    }

    void addDependencySelectionCallback(Consumer<List<Dependency> > listener) {
        violationTable.getSelectionModel().addListSelectionListener(
                e -> {
                    if(!e.getValueIsAdjusting()) {
                        listener.accept(getSelectedDependencyList());
                    }
                });
        violationTable.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                if(! e.isTemporary())
                    listener.accept(getSelectedDependencyList());
            }

        });
    }

    private List<Dependency> getSelectedDependencyList() {
        final List<Dependency> selectedDependencies =
                violations.isEmpty()
                ? Collections.emptyList()
                : IntStream.of(violationTable.getSelectedRows())
        .mapToObj(row -> violationTable.getValueAt(row, 0))
        .map(violations::get)
        .filter(x -> x != null)
        .collect(Collectors.toList());
        return selectedDependencies;
    }

}
