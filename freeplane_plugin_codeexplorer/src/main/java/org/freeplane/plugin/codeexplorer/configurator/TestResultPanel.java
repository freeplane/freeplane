package org.freeplane.plugin.codeexplorer.configurator;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.core.ui.textchanger.TranslatedElementFactory;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.codeexplorer.archunit.ArchUnitServer;
import org.freeplane.plugin.codeexplorer.task.ParsedConfiguration;

import com.tngtech.archunit.freeplane.extension.ArchTestResult;

class TestResultPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private DefaultTableModel ruleTableModel;
    private DefaultTableModel violationTableModel;
    private JTable ruleTable;
    private JTable violationTable;
    private final CodeProjectController codeProjectController;
    private final ArchUnitServer archUnitServer;


    TestResultPanel(CodeProjectController codeProjectController, ArchUnitServer archUnitServer) {
        this.codeProjectController = codeProjectController;
        this.archUnitServer = archUnitServer;
        archUnitServer.setCallback(this::testResultAdded);
        initializeComponents();
        updateRuleTable();
    }

    private void testResultAdded(ArchTestResult result) {
        addNewConfiguration(result.violatedRuleDescription);
    }


    private void updateRuleTable() {
        ruleTableModel.setRowCount(0); // Clear existing data
        for (ArchTestResult rule : submittedConfigurations()) {
            ruleTableModel.addRow(new Object[]{rule.violatedRuleDescription});
        }
        WrappingTableCellRenderer.adjustRowHeights(ruleTable);
    }


    private void initializeComponents() {
        createPanels();
    }


    private JComponent createRulePanel() {
        ruleTableModel = createEmptyTableModel();
        ruleTable = new JTable(ruleTableModel);
        final WrappingTableCellRenderer renderer = new WrappingTableCellRenderer();
        ruleTable.setDefaultRenderer(Object.class, renderer);
        ruleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane ruleTableScrollPane = new JScrollPane(ruleTable);

        ruleTable.getSelectionModel().addListSelectionListener(e -> updateViolations());

        return ruleTableScrollPane;
     }

    @SuppressWarnings("serial")
    private JComponent createViolationsPane(JLabel paneLabel) {

        violationTableModel = createEmptyTableModel();
        violationTable = new AutoResizedTable(violationTableModel);
        violationTable.getTableHeader().setVisible(false);
        violationTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
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

        // Use CardLayout to switch views
        CardLayout cardLayout = new CardLayout();
        JPanel cardPanel = new JPanel(cardLayout);

        // Add the table scroll pane to the card panel
        cardPanel.add(violationsTableScrollPane, "Violations");

        // Create a help text component and add it to the card panel
        JTextArea helpText = new JTextArea(ParsedConfiguration.HELP);
        helpText.setEditable(false); // make it read-only if it's a text area
        cardPanel.add(new JScrollPane(helpText), "Help");

        String violationsHeaderText = TextUtils.getText("code.violations");
        paneLabel.setText(violationsHeaderText);
        return cardPanel;
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
        ArchTestResult rule = getSelectedConfiguration();
        if (rule !=  null) {
            for (String description : rule.violationDescriptions) {
                violationTableModel.addRow(new Object[]{description});
            }
        }
        WrappingTableCellRenderer.adjustRowHeights(violationTable);
    }

    private void exploreSelectedConfiguration() {
        codeProjectController.exploreConfiguration(getSelectedConfiguration());
    }

    ArchTestResult getSelectedConfiguration() {
        int selectedConfigurationIndex = getSelectedConfigurationIndex();
        ArchTestResult selectedConfiguration = getConfiguration(selectedConfigurationIndex);
        return selectedConfiguration;
    }

    private ArchTestResult getConfiguration(int selectedConfigurationIndex) {
        final List<ArchTestResult> submittedConfigurations = submittedConfigurations();
        if(selectedConfigurationIndex >= 0 && selectedConfigurationIndex < submittedConfigurations.size()) {
            return submittedConfigurations.get(selectedConfigurationIndex);
        } else
            return null;
    }


    private List<ArchTestResult> submittedConfigurations() {
        return archUnitServer.getSubmittedConfigurations();
    }

    private int getSelectedConfigurationIndex() {
        return ruleTable.getSelectedRow();
    }

    private void addNewConfiguration(String violatedRuleDescription) {
        ruleTableModel.addRow(new Object[]{violatedRuleDescription});
        final int row = ruleTable.getRowCount() - 1;
        WrappingTableCellRenderer.adjustRowHeight(ruleTable, row);
        if(row == 0)
            ruleTable.addRowSelectionInterval(row, row);
    }

    private void deleteSelectedConfiguration() {
        int selectedRow = getSelectedConfigurationIndex();
        if (selectedRow >= 0) {
            ruleTableModel.removeRow(selectedRow);
            submittedConfigurations().remove(selectedRow);
            int rowCount = ruleTableModel.getRowCount();
            if(selectedRow < rowCount)
                ruleTable.setRowSelectionInterval(selectedRow, selectedRow);
            else if (rowCount > 0)
                ruleTable.setRowSelectionInterval(rowCount-1, rowCount-1);
            updateViolations();
        }
    }

    private void cancelAnalysis() {
        codeProjectController.cancelAnalysis();
    }

    private void createPanels() {
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        JLabel rulesLabel = new JLabel(TextUtils.getText("code.rules"));
        JComponent ruleurationTableToolbar = createRuleTableToolbar();
        JComponent rulesPanel = createRulePanel();
        JLabel violationsLabel = new JLabel();
        JComponent violationsToolbar = createViolationsButtons();
        JComponent violationsPane = createViolationsPane(violationsLabel);


        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1; // Span across all columns

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor=GridBagConstraints.CENTER;
        add(rulesLabel, gbc);

        gbc.gridy = 1;
        gbc.anchor=GridBagConstraints.LINE_START;
        add(ruleurationTableToolbar, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor=GridBagConstraints.CENTER;
        add(violationsLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor=GridBagConstraints.LINE_START;
        add(violationsToolbar, gbc);

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

    private JComponent createRuleTableToolbar() {
        FreeplaneToolBar toolbar = new FreeplaneToolBar(SwingConstants.HORIZONTAL);
        JButton deleteConfigurationButton = TranslatedElementFactory.createButtonWithIcon("code.delete");
        deleteConfigurationButton.addActionListener(e -> deleteSelectedConfiguration());
        JButton clearConfigurationsButton = TranslatedElementFactory.createButtonWithIcon("code.clear");
        clearConfigurationsButton.addActionListener(e -> clear());
        toolbar.add(deleteConfigurationButton);
        toolbar.add(clearConfigurationsButton);
        return toolbar;
    }

    private void clear() {
        violationTableModel.setRowCount(0);
        ruleTableModel.setRowCount(0);
    }

    private JComponent createViolationsButtons() {
        FreeplaneToolBar toolbar = new FreeplaneToolBar(SwingConstants.HORIZONTAL);
        JButton exploreConfigurationButton = TranslatedElementFactory.createButtonWithIcon("code.explore");
        exploreConfigurationButton.addActionListener(e -> exploreSelectedConfiguration());

        JButton cancelButton = TranslatedElementFactory.createButtonWithIcon("code.cancel");
        cancelButton.addActionListener(e -> cancelAnalysis());

        JComponent panelButtons[] = {exploreConfigurationButton, cancelButton};
        Stream.of(panelButtons).forEach(button -> {
            toolbar.add(button);
        });

        JButton enablingButtons[] = {};

        Stream.of(enablingButtons).forEach(button -> {
            button.setEnabled(false);
        });

        Runnable enableButtons = () -> {
            boolean enable = ruleTable.getSelectionModel().getMinSelectionIndex() >= 0;
            Stream.of(enablingButtons).forEach(button -> button.setEnabled(enable));
        };

        ruleTable.getSelectionModel().addListSelectionListener(l -> enableButtons.run());
        return toolbar;

    }
}
