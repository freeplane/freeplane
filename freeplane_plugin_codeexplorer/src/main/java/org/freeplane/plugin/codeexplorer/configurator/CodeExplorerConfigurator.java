package org.freeplane.plugin.codeexplorer.configurator;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.textchanger.TranslatedElementFactory;
import org.freeplane.plugin.codeexplorer.task.CodeExplorerConfiguration;
import org.freeplane.plugin.codeexplorer.task.CodeExplorerConfigurations;
import org.freeplane.plugin.codeexplorer.task.ConfigurationChange;
import org.freeplane.plugin.codeexplorer.task.ParsedConfiguration;

class CodeExplorerConfigurator extends JPanel {

    private static final long serialVersionUID = 1L;
    private DefaultTableModel configTableModel;
    private DefaultTableModel locationsTableModel;
    private JTable configTable;
    private JTable locationsTable;
    private final CodeProjectController codeProjectController;
    private JTextArea rules;
    private JFileChooser fileChooser;
    private ConfigurationChange configurationChange;


    CodeExplorerConfigurator(CodeProjectController codeProjectController) {
        this.codeProjectController = codeProjectController;
        configurationChange = ConfigurationChange.CODE_BASE;
        initializeComponents();
        updateConfigurationsTable(explorerConfigurations());
    }


    private void updateConfigurationsTable(CodeExplorerConfigurations explorerConfigurations) {
        configTableModel.setRowCount(0); // Clear existing data
        for (CodeExplorerConfiguration config : explorerConfigurations.getConfigurations()) {
            configTableModel.addRow(new Object[]{config.getProjectName()});
        }
    }


    private void initializeComponents() {
        createPanels();
        createFileChooser();
    }


    private void createFileChooser() {
        fileChooser = UITools.newFileChooser(null);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("jar", "jar");
        fileChooser.setFileFilter(filter);
    }

    private JPanel createConfigurationsPanel() {
        JPanel configPanel = new JPanel();
        configPanel.setLayout(new BorderLayout());

        JLabel configurationsLabel = new JLabel("Configurations", SwingConstants.CENTER);
        configurationsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        configPanel.add(configurationsLabel, BorderLayout.NORTH);

        configTableModel = new DefaultTableModel(new Object[]{""}, 0);
        configTable = new JTable(configTableModel);
        configTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane configTableScrollPane = new JScrollPane(configTable);
        configPanel.add(configTableScrollPane, BorderLayout.CENTER);

        configTable.getSelectionModel().addListSelectionListener(e -> updateConfiguration());

        configTableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int firstRow = e.getFirstRow();
                if (e.getType() == TableModelEvent.UPDATE && firstRow >= 0) {
                    int lastRow = e.getLastRow();
                    updateConfigurationNames(firstRow, lastRow);
                }
            }
        });
        return configPanel;
    }

    @SuppressWarnings("serial")
    private JPanel createLocationsPanel() {
        JPanel locationsPanel = new JPanel();
        locationsPanel.setLayout(new BorderLayout());

        JLabel locationsLabel = new JLabel("Locations", SwingConstants.CENTER);
        locationsPanel.add(locationsLabel, BorderLayout.NORTH);

        locationsTableModel = new DefaultTableModel(new Object[]{""}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
               return false;
            }
        };
        locationsTable = new AutoResizedTable(locationsTableModel);
        locationsTable.getTableHeader().setVisible(false);
        locationsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        CellRendererWithTooltip rightAlignRenderer = new CellRendererWithTooltip();
        TableColumn locationsColumn = locationsTable.getColumnModel().getColumn(0);
        locationsColumn.setCellRenderer(rightAlignRenderer);
        JScrollPane locationsTableScrollPane = new JScrollPane(locationsTable);
        locationsTableScrollPane.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                locationsTable.revalidate();
                locationsTable.repaint();
            }

        });
        locationsPanel.add(locationsTableScrollPane, BorderLayout.CENTER);

        return locationsPanel;
    }

    private JPanel createRulesPanel() {
        JPanel rulesPanel = new JPanel();
        rulesPanel.setLayout(new BorderLayout());

        JLabel rulesLabel = new JLabel("Rules", SwingConstants.CENTER);
        rulesLabel.setHorizontalAlignment(SwingConstants.CENTER);
        rulesPanel.add(rulesLabel, BorderLayout.NORTH);

        rules = new JTextArea();
        JScrollPane rulesScrollPane = new JScrollPane(rules);
        rulesPanel.add(rulesScrollPane, BorderLayout.CENTER);

        return rulesPanel;
    }


    private void updateConfigurationNames(int firstRow, int lastRow) {
        for (int row = firstRow; row <= lastRow; row++) {
            updateConfigurationName(row);
        }
    }

    private void updateConfigurationName(int row) {
        String projectName = ((String) configTableModel.getValueAt(row, 0)).trim();
        CodeExplorerConfiguration config = getConfiguration(row);
        config.setProjectName(projectName);
    }

    private void updateConfiguration() {
        locationsTableModel.setRowCount(0); // Clear existing data
        int selectedRow = getSelectedConfigurationIndex();
        if (selectedRow >= 0) {
            CodeExplorerConfiguration config = getConfiguration(selectedRow);
            for (File location : config.getLocations()) {
                locationsTableModel.addRow(new Object[]{location.getAbsolutePath()});
            }
            rules.setText(config.getConfigurationRules());
        }
        else
            rules.setText("");
        configurationChange = ConfigurationChange.CODE_BASE;
    }

    private void exploreSelectedConfiguration() {
        setConfigurationRules();
        codeProjectController.exploreConfiguration(getSelectedConfiguration());
        configurationChange = ConfigurationChange.SAME;
    }

    CodeExplorerConfiguration getSelectedConfiguration() {
        int selectedConfigurationIndex = getSelectedConfigurationIndex();
        CodeExplorerConfiguration selectedConfiguration = getConfiguration(selectedConfigurationIndex);
        return selectedConfiguration;
    }

    private CodeExplorerConfiguration getConfiguration(int selectedConfigurationIndex) {
        if(selectedConfigurationIndex >= 0)
            return explorerConfigurations().getConfigurations().get(selectedConfigurationIndex);
        else
            return null;
    }

    private int getSelectedConfigurationIndex() {
        return configTable.getSelectedRow();
    }

    private void addNewConfiguration() {
        CodeExplorerConfiguration newConfig = new CodeExplorerConfiguration();
        explorerConfigurations().getConfigurations().add(newConfig);
        configTableModel.addRow(new Object[]{newConfig.getProjectName()});
        int newRow = configTable.getRowCount() - 1;
        configTable.setRowSelectionInterval(newRow, newRow);
        configTable.editCellAt(newRow, 0);
        configTable.getEditorComponent().requestFocusInWindow();
    }

    private void deleteSelectedConfiguration() {
        int selectedRow = getSelectedConfigurationIndex();
        if (selectedRow >= 0) {
            configTableModel.removeRow(selectedRow);
            explorerConfigurations().getConfigurations().remove(selectedRow);
            int rowCount = configTableModel.getRowCount();
            if(selectedRow < rowCount)
                configTable.setRowSelectionInterval(selectedRow, selectedRow);
            else if (rowCount > 0)
                configTable.setRowSelectionInterval(rowCount-1, rowCount-1);
            updateConfiguration();
        }
    }





    private void moveSelectedLocationsToTheTop() {
        int[] selectedRows = locationsTable.getSelectedRows();
        Arrays.sort(selectedRows);
        for (int i = 0; i < selectedRows.length; i++) {
            locationsTableModel.moveRow(selectedRows[i], selectedRows[i], i);
        }
        locationsTable.getSelectionModel().setValueIsAdjusting(true);
        locationsTable.clearSelection();
        for (int i = 0; i < selectedRows.length; i++) {
            locationsTable.addRowSelectionInterval(i, i);
        }
        locationsTable.getSelectionModel().setValueIsAdjusting(false);
        updateSelectedConfigurationLocations();
    }

    private void moveSelectedLocationsUp() {
        int[] selectedRows = locationsTable.getSelectedRows();
        if (selectedRows.length > 0 && selectedRows[0] > 0) {
            for (int i = 0; i < selectedRows.length; i++) {
                int selectedIndex = selectedRows[i];
                locationsTableModel.moveRow(selectedIndex, selectedIndex, selectedIndex - 1);
            }
            locationsTable.getSelectionModel().setValueIsAdjusting(true);
            locationsTable.clearSelection();
            for (int selectedIndex : selectedRows) {
                locationsTable.addRowSelectionInterval(selectedIndex - 1, selectedIndex - 1);
            }
            locationsTable.getSelectionModel().setValueIsAdjusting(false);
            updateSelectedConfigurationLocations();
        }
    }

    private void moveSelectedLocationsDown() {
        int[] selectedRows = locationsTable.getSelectedRows();
        if (selectedRows.length > 0 && selectedRows[selectedRows.length - 1] < locationsTableModel.getRowCount() - 1) {
            for (int i = selectedRows.length - 1; i >= 0; i--) {
                int selectedIndex = selectedRows[i];
                locationsTableModel.moveRow(selectedIndex, selectedIndex, selectedIndex + 1);
            }
            locationsTable.getSelectionModel().setValueIsAdjusting(true);
            locationsTable.clearSelection();
            for (int selectedIndex : selectedRows) {
                locationsTable.addRowSelectionInterval(selectedIndex + 1, selectedIndex + 1);
            }
            locationsTable.getSelectionModel().setValueIsAdjusting(false);
            updateSelectedConfigurationLocations();
        }
    }

    private void moveSelectedLocationsToTheBottom() {
        int[] selectedRows = locationsTable.getSelectedRows();
        Arrays.sort(selectedRows);
        int rowCount = locationsTableModel.getRowCount();
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            int selectedIndex = selectedRows[i];
            locationsTableModel.moveRow(selectedIndex, selectedIndex, rowCount - 1 - (selectedRows.length - 1 - i));
        }
        locationsTable.getSelectionModel().setValueIsAdjusting(true);
        locationsTable.clearSelection();
        for (int i = 0; i < selectedRows.length; i++) {
            int newRow = rowCount - selectedRows.length + i;
            locationsTable.addRowSelectionInterval(newRow, newRow);
        }
        locationsTable.getSelectionModel().setValueIsAdjusting(false);
        updateSelectedConfigurationLocations();
    }


    private void updateSelectedConfigurationLocations() {
        int selectedConfigRow = getSelectedConfigurationIndex();
        if (selectedConfigRow >= 0) {
            CodeExplorerConfiguration config = getConfiguration(selectedConfigRow);
            config.removeAllLocations();
            for(int row = 0; row < locationsTableModel.getRowCount(); row++)
                config.addLocation((String)locationsTableModel.getValueAt(row, 0));
        }
    }


    private void removeSelectedLocations() {
        int[] selectedRows = locationsTable.getSelectedRows();
        Arrays.sort(selectedRows);
        int removedRowCount = 0;
        for (int selectedIndex : selectedRows) {
            int row = selectedIndex - removedRowCount;
            int selectedConfigRow = getSelectedConfigurationIndex();
            if (selectedConfigRow >= 0) {
                CodeExplorerConfiguration config = getConfiguration(selectedConfigRow);
                config.removeLocation(locationsTableModel.getValueAt(row, 0).toString());
            }
            locationsTableModel.removeRow(row);
            removedRowCount++;
        }
        if(removedRowCount > 0)
            configurationChange = ConfigurationChange.CODE_BASE;
    }


    private void addJarsAndFolders() {
        if(configTable.getRowCount() == 0)
            addNewConfiguration();
        int selectedConfigRow = getSelectedConfigurationIndex();
        CodeExplorerConfiguration selectedConfig = getConfiguration(selectedConfigRow);
        int selectedRow = locationsTable.getSelectedRow();
        if(selectedRow >= 0) {
            File selectedFile = new File(locationsTable.getValueAt(selectedRow, 0).toString());
            File selectedDirectory = selectedFile.getParentFile();
            if(selectedDirectory != null) {
                fileChooser.setSelectedFile(null);
                fileChooser.setCurrentDirectory(selectedDirectory);
            }
        }
        int option = fileChooser.showOpenDialog(CodeExplorerConfigurator.this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files) {
                String path = file.getAbsolutePath();
                if(! selectedConfig.containsLocation(path)) {
                    locationsTableModel.addRow(new Object[]{path});
                    selectedConfig.addLocation(file);
                    configurationChange = ConfigurationChange.CODE_BASE;
                }
            }
        }
    }

    private void cancelAnalysis() {
        codeProjectController.cancelAnalysis();
    }

    private void applyConfigurationRules() {
        setConfigurationRules();
        switch(configurationChange) {
        case CODE_BASE:
            exploreSelectedConfiguration();
            break;
        case JUDGE:
            codeProjectController.updateJudge();
            break;
        case SAME:
            break;
        }
        configurationChange = ConfigurationChange.SAME;
    }
    private void setConfigurationRules() {
        CodeExplorerConfiguration selectedConfiguration = getSelectedConfiguration();
        if(selectedConfiguration != null) {
            String ruleSpecification = rules.getText();
            if(! selectedConfiguration.getConfigurationRules().equals(ruleSpecification)) {
                try {
                    ConfigurationChange status = selectedConfiguration.applyConfigurationRules(ruleSpecification);
                    configurationChange = ConfigurationChange.max(configurationChange, status);
                } catch (IllegalArgumentException e) {
                    String text = e.getMessage();
                    ParsedConfiguration.showHelp(text);
                }
            }
        }
    }

    private void createPanels() {
        JPanel configurationsPanel = createConfigurationsPanel();
        JPanel locationsPanel = createLocationsPanel();
        JPanel rulesPanel = createRulesPanel();
        JPanel configButtonsPanel = createConfigButtons();

        JPanel unifiedButtonsPanel = createUnifiedButtonsPanel();

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1; // Span across all columns
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor=GridBagConstraints.LINE_START;
        add(configButtonsPanel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Span across all columns
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        add(unifiedButtonsPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 1;
        add(configurationsPanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 2;
        add(locationsPanel, gbc);

        gbc.gridx = 2;
        gbc.weightx = 1;
        add(rulesPanel, gbc);
    }

    private JPanel createConfigButtons() {
        JPanel configButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addConfigurationButton = TranslatedElementFactory.createButtonWithIcon("code.add");
        addConfigurationButton.addActionListener(e -> addNewConfiguration());
        JButton deleteConfigurationButton = TranslatedElementFactory.createButtonWithIcon("code.delete");
        deleteConfigurationButton.addActionListener(e -> deleteSelectedConfiguration());
        configButtonsPanel.add(addConfigurationButton);
        configButtonsPanel.add(deleteConfigurationButton);
        return configButtonsPanel;
    }

    private JPanel createUnifiedButtonsPanel() {
        JPanel unifiedButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton applyButton = TranslatedElementFactory.createButtonWithIcon("code.apply");
        applyButton.addActionListener(e ->applyConfigurationRules());

        JButton exploreConfigurationButton = TranslatedElementFactory.createButtonWithIcon("code.explore");
        exploreConfigurationButton.addActionListener(e -> exploreSelectedConfiguration());

        JButton cancelButton = TranslatedElementFactory.createButtonWithIcon("code.cancel");
        cancelButton.addActionListener(e -> cancelAnalysis());

        JButton revertButton = TranslatedElementFactory.createButtonWithIcon("code.revert");
        revertButton.addActionListener(e ->
            rules.setText(getSelectedConfiguration().getConfigurationRules())
        );

        JButton addJarsButton = TranslatedElementFactory.createButtonWithIcon("code.add_location");
        addJarsButton.addActionListener(e1 -> addJarsAndFolders());

        JButton btnMoveToTheTop = TranslatedElementFactory.createButtonWithIcon("code.move_to_the_top");
        btnMoveToTheTop.addActionListener(e4 -> moveSelectedLocationsToTheTop());

        JButton btnMoveUp = TranslatedElementFactory.createButtonWithIcon("code.move_up");
        btnMoveUp.addActionListener(e3 -> moveSelectedLocationsUp());

        JButton btnMoveDown = TranslatedElementFactory.createButtonWithIcon("code.move_down");
        btnMoveDown.addActionListener(e5 -> moveSelectedLocationsDown());

        JButton btnMoveToTheBottom = TranslatedElementFactory.createButtonWithIcon("code.move_to_the_bottom");
        btnMoveToTheBottom.addActionListener(e2 -> moveSelectedLocationsToTheBottom());

        JButton removeLocationsButton = TranslatedElementFactory.createButtonWithIcon("code.remove_location");
        removeLocationsButton.addActionListener(e6 -> removeSelectedLocations());

        JButton helpButton = TranslatedElementFactory.createButtonWithIcon("code.help");
        helpButton.addActionListener(e ->
            ParsedConfiguration.showHelp("")
        );

        JButton panelButtons[] = {exploreConfigurationButton, applyButton, cancelButton, revertButton, addJarsButton, btnMoveToTheTop, btnMoveUp, btnMoveDown, btnMoveToTheBottom, removeLocationsButton, helpButton};
        Stream.of(panelButtons).forEach(button -> {
            unifiedButtonsPanel.add(button);
        });

        JButton enablingButtons[] = {addJarsButton, btnMoveToTheTop, btnMoveUp, btnMoveDown, btnMoveToTheBottom, removeLocationsButton, applyButton, revertButton};

        Stream.of(enablingButtons).forEach(button -> {
            button.setEnabled(false);
        });

        configTable.getSelectionModel().addListSelectionListener(l1 -> {
            boolean isSelectionValid1 = ((ListSelectionModel)l1.getSource()).getMinSelectionIndex() >= 0;
            Stream.of(enablingButtons).forEach(button -> button.setEnabled(isSelectionValid1));
        });
        return unifiedButtonsPanel;
    }

    public List<File> getSelectedLocations() {
        List<File> paths = new ArrayList<>();
        for (int i = 0; i < locationsTableModel.getRowCount(); i++) {
            paths.add(new File(locationsTableModel.getValueAt(i, 0).toString()));
        }
        return paths;
    }


    private CodeExplorerConfigurations explorerConfigurations() {
        return codeProjectController.explorerConfigurations();
    }
}
