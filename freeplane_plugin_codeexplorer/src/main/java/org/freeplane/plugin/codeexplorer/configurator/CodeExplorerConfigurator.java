package org.freeplane.plugin.codeexplorer.configurator;

import java.awt.Component;
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

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.textchanger.TranslatedElementFactory;
import org.freeplane.plugin.codeexplorer.task.CodeExplorerConfiguration;
import org.freeplane.plugin.codeexplorer.task.CodeExplorerConfigurations;
import org.freeplane.plugin.codeexplorer.task.DependencyJudge;

class CodeExplorerConfigurator extends JPanel {

    private static final long serialVersionUID = 1L;
    private DefaultTableModel configTableModel;
    private DefaultTableModel locationsTableModel;
    private JTable configTable;
    private JTable locationsTable;
    private final CodeProjectController codeProjectController;
    private JTextArea rules;
    private JFileChooser fileChooser;


    CodeExplorerConfigurator(CodeProjectController codeProjectController) {
        this.codeProjectController = codeProjectController;
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
        createConfigurationsPanel();
        createLocationsPanel();
        layoutPanels();
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

        JPanel configPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = createGridBagConstraints();

        JLabel locationsLabel = new JLabel("Configurations");
        locationsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        addComponentToPanel(locationsLabel, configPanel, gbc, 0, 0, 1, 0);

        configTableModel = new DefaultTableModel(new Object[]{""}, 0);
        configTable = new JTable(configTableModel);
        configTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane configTableScrollPane = new JScrollPane(configTable);
        addComponentToPanel(configTableScrollPane, configPanel, gbc, 0, 1, 1, 1);

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

        JPanel configButtonsPanel = createConfigButtonsPanel();
        addComponentToPanel(configButtonsPanel, configPanel, gbc, 0, 2, 1, 0);
        return configPanel;
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
    }

    private JPanel createConfigButtonsPanel() {
        JPanel configButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addConfigurationButton = TranslatedElementFactory.createButton("code.add");
        addConfigurationButton.addActionListener(e -> addNewConfiguration());
        JButton deleteConfigurationButton = TranslatedElementFactory.createButton("code.delete");
        deleteConfigurationButton.addActionListener(e -> deleteSelectedConfiguration());
        JButton exploreConfigurationButton = TranslatedElementFactory.createButton("code.explore");
        exploreConfigurationButton.addActionListener(e -> exploreSelectedConfiguration());
        configButtonsPanel.add(addConfigurationButton);
        configButtonsPanel.add(deleteConfigurationButton);
        configButtonsPanel.add(exploreConfigurationButton);
        return configButtonsPanel;
    }

    private void exploreSelectedConfiguration() {
        setConfigurationRules();
        codeProjectController.exploreConfiguration(getSelectedConfiguration());
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

    @SuppressWarnings("serial")
    private JPanel createLocationsPanel() {
        JPanel locationsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = createGridBagConstraints();

        JLabel locationsLabel = new JLabel("Locations");
        locationsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        addComponentToPanel(locationsLabel, locationsPanel, gbc, 0, 0, 1, 0);

        locationsTableModel = new DefaultTableModel(new Object[]{""}, 0);
        locationsTable = new AutoResizedTable(locationsTableModel);
        locationsTable.getTableHeader().setVisible(false);
        locationsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        CellRendererWithTooltip rightAlignRenderer = new CellRendererWithTooltip();
        locationsTable.getColumnModel().getColumn(0).setCellRenderer(rightAlignRenderer);
        JScrollPane locationsTableScrollPane = new JScrollPane(locationsTable);
        locationsTableScrollPane.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                locationsTable.revalidate();
                locationsTable.repaint();
            }

        });
        addComponentToPanel(locationsTableScrollPane, locationsPanel, gbc, 0, 1, 1, 1);

        JPanel locationsButtonsPanel = createLocationsButtonsPanel();
        addComponentToPanel(locationsButtonsPanel, locationsPanel, gbc, 0, 2, 1, 0);
        return locationsPanel;
    }




    private JPanel createLocationsButtonsPanel() {
        JPanel locationsButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addJarsButton = TranslatedElementFactory.createButton("code.add_location");
        addJarsButton.addActionListener(e -> addJarsAndFolders());
        JButton btnMoveToTheTop = TranslatedElementFactory.createButtonWithIcon("code.move_to_the_top");
        btnMoveToTheTop.addActionListener(e -> moveSelectedLocationsToTheTop());
        JButton btnMoveUp = TranslatedElementFactory.createButtonWithIcon("code.move_up");
        btnMoveUp.addActionListener(e -> moveSelectedLocationsUp());
        JButton btnMoveDown = TranslatedElementFactory.createButtonWithIcon("code.move_down");
        btnMoveDown.addActionListener(e -> moveSelectedLocationsDown());
        JButton btnMoveToTheBottom = TranslatedElementFactory.createButtonWithIcon("code.move_to_the_bottom");
        btnMoveToTheBottom.addActionListener(e -> moveSelectedLocationsToTheBottom());
        JButton removeLocationsButton = TranslatedElementFactory.createButton("code.remove_location");
        removeLocationsButton.addActionListener(e -> removeSelectedLocations());

        JButton buttons[] = {addJarsButton, btnMoveToTheTop, btnMoveUp, btnMoveDown, btnMoveToTheBottom, removeLocationsButton};

        Stream.of(buttons).forEach(button -> {
            locationsButtonsPanel.add(button);
            button.setEnabled(false);
        });

        configTable.getSelectionModel().addListSelectionListener(l -> {
            boolean isSelectionValid = ((ListSelectionModel)l.getSource()).getMinSelectionIndex() >= 0;
            Stream.of(buttons).forEach(button -> button.setEnabled(isSelectionValid));
        });
        return locationsButtonsPanel;
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
                }
            }
        }
    }


    private JPanel createRulesPanel() {
        JPanel rulesPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = createGridBagConstraints();

        JLabel rulesLabel = new JLabel("Rules");
        rulesLabel.setHorizontalAlignment(SwingConstants.CENTER);
        addComponentToPanel(rulesLabel, rulesPanel, gbc, 0, 0, 1, 0);

        rules = new JTextArea();
        JScrollPane rulesTableScrollPane = new JScrollPane(rules);
        addComponentToPanel(rulesTableScrollPane, rulesPanel, gbc, 0, 1, 1, 1);

        JPanel rulesButtonsPanel = createRulesButtonsPanel();
        addComponentToPanel(rulesButtonsPanel, rulesPanel, gbc, 0, 2, 1, 0);

        return rulesPanel;
    }

    private JPanel createRulesButtonsPanel() {
        JPanel rulesButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(e ->
            setConfigurationRules()
        );
        JButton revertButton = new JButton("Revert");
        revertButton.addActionListener(e ->
            rules.setText(getSelectedConfiguration().getConfigurationRules())
        );
        JButton helpButton = new JButton("Help");
        helpButton.addActionListener(e ->
            DependencyJudge.showHelp("")
        );
        rulesButtonsPanel.add(applyButton);
        rulesButtonsPanel.add(revertButton);
        rulesButtonsPanel.add(helpButton);

        applyButton.setEnabled(false);
        revertButton.setEnabled(false);

        configTable.getSelectionModel().addListSelectionListener(l -> {
            boolean isSelectionValid = ((ListSelectionModel)l.getSource()).getMinSelectionIndex() >= 0;
            applyButton.setEnabled(isSelectionValid);
            revertButton.setEnabled(isSelectionValid);
        });
        return rulesButtonsPanel;
    }

    private void setConfigurationRules() {
        CodeExplorerConfiguration selectedConfiguration = getSelectedConfiguration();
        if(selectedConfiguration != null) {
            String ruleSpecification = rules.getText();
            if(! selectedConfiguration.getConfigurationRules().equals(ruleSpecification)) {
                try {
                    selectedConfiguration.setConfigurationRules(ruleSpecification);
                    codeProjectController.setJudge(selectedConfiguration.getDependencyJudge());

                } catch (IllegalArgumentException e) {
                    String text = e.getMessage();
                    DependencyJudge.showHelp(text);
                }
            }
        }
    }

    private void layoutPanels() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(createConfigurationsPanel(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 2;
        add(createLocationsPanel(), gbc);

        gbc.gridx = 2;
        gbc.weightx = 1;
        add(createRulesPanel(), gbc);
    }

    private GridBagConstraints createGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        return gbc;
    }

    private void addComponentToPanel(Component component, JPanel panel, GridBagConstraints gbc,
                                     int gridx, int gridy, double weightx, double weighty) {
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        panel.add(component, gbc);
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
