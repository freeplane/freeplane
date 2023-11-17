package org.freeplane.main.codeexplorermode;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.importer.ClassFileImporter;

public class CodeExplorerConfigurator extends JPanel {

    private static final long serialVersionUID = 1L;
    private DefaultTableModel configurationsModel;
    private DefaultTableModel locationsModel;
    private JTable configTable;
    private JTable locationTable;
    private CodeExplorerConfigurations explorerConfigurations;

    public CodeExplorerConfigurator(CodeExplorerConfigurations explorerConfigurations) {
        this.explorerConfigurations = explorerConfigurations;
        initializeComponents();
        loadConfigurations();
    }

    private void initializeComponents() {
        createConfigurationsPanel();
        createLocationsPanel();
        layoutPanels();
    }

    private JPanel createConfigurationsPanel() {
        JPanel configPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = createGridBagConstraints();

        configurationsModel = new DefaultTableModel(new Object[]{"Configurations"}, 0);
        configTable = new JTable(configurationsModel);
        configTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane configTableScrollPane = new JScrollPane(configTable);
        addComponentToPanel(configTableScrollPane, configPanel, gbc, 0, 0, 1, 1);

        configTable.getSelectionModel().addListSelectionListener(e -> updateLocationsModel());

        configurationsModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    updateConfigurationsModel();
                }
            }
        });

        JPanel configButtonsPanel = createConfigButtonsPanel();
        addComponentToPanel(configButtonsPanel, configPanel, gbc, 0, 1, 1, 0);
        return configPanel;
    }


    private void loadConfigurations() {
        configurationsModel.setRowCount(0); // Clear existing data
        for (CodeExplorerConfiguration config : explorerConfigurations.getConfigurations()) {
            configurationsModel.addRow(new Object[]{config.getProjectName()});
        }
    }

    private void updateConfigurationsModel() {
        int selectedRow = getSelectedConfigurationIndex();
        if (selectedRow >= 0) {
            String projectName = (String) configurationsModel.getValueAt(selectedRow, 0);
            CodeExplorerConfiguration config = getConfiguration(selectedRow);
            config.setProjectName(projectName);
        }
    }

    private void updateLocationsModel() {
        locationsModel.setRowCount(0); // Clear existing data
        int selectedRow = getSelectedConfigurationIndex();
        if (selectedRow >= 0) {
            CodeExplorerConfiguration config = getConfiguration(selectedRow);
            for (File location : config.getLocations()) {
                locationsModel.addRow(new Object[]{location.getAbsolutePath()});
            }
        }
    }

    private JPanel createConfigButtonsPanel() {
        JPanel configButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addConfigurationButton = new JButton("Add");
        addConfigurationButton.addActionListener(e -> addNewConfiguration());
        JButton deleteConfigurationButton = new JButton("Delete");
        deleteConfigurationButton.addActionListener(e -> deleteSelectedConfiguration());
        JButton exploreConfigurationButton = new JButton("Explore");
        exploreConfigurationButton.addActionListener(e -> exploreSelectedConfiguration());
        configButtonsPanel.add(addConfigurationButton);
        configButtonsPanel.add(deleteConfigurationButton);
        configButtonsPanel.add(exploreConfigurationButton);
        return configButtonsPanel;
    }

    private void exploreSelectedConfiguration() {
        CodeMapController mapController = (CodeMapController) Controller.getCurrentModeController().getMapController();
        int selectedConfigurationIndex = getSelectedConfigurationIndex();
        mapController.explore(getConfiguration(selectedConfigurationIndex));
    }

    private CodeExplorerConfiguration getConfiguration(int selectedConfigurationIndex) {
        if(selectedConfigurationIndex >= 0)
            return explorerConfigurations.getConfigurations().get(selectedConfigurationIndex);
        else
            return null;
    }

    private int getSelectedConfigurationIndex() {
        return configTable.getSelectedRow();
    }

    private void addNewConfiguration() {
        CodeExplorerConfiguration newConfig = new CodeExplorerConfiguration("", new ArrayList<>());
        explorerConfigurations.getConfigurations().add(newConfig);
        configurationsModel.addRow(new Object[]{newConfig.getProjectName()});
        if(configurationsModel.getRowCount() == 1)
            configTable.setRowSelectionInterval(0, 0);
        configTable.editCellAt(configTable.getRowCount() - 1, 0);
        configTable.getEditorComponent().requestFocusInWindow();
    }

    private void deleteSelectedConfiguration() {
        int selectedRow = getSelectedConfigurationIndex();
        if (selectedRow >= 0) {
            configurationsModel.removeRow(selectedRow);
            explorerConfigurations.getConfigurations().remove(selectedRow);
            int rowCount = configurationsModel.getRowCount();
            if(selectedRow < rowCount)
                configTable.setRowSelectionInterval(selectedRow, selectedRow);
            else if (rowCount > 0)
                configTable.setRowSelectionInterval(rowCount-1, rowCount-1);
            updateLocationsModel();
        }
    }

    private JPanel createLocationsPanel() {
        JPanel locationsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = createGridBagConstraints();

        locationsModel = new DefaultTableModel(new Object[]{"Locations"}, 0);
        locationTable = new JTable(locationsModel);
        locationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane locationsTableScrollPane = new JScrollPane(locationTable);
        addComponentToPanel(locationsTableScrollPane, locationsPanel, gbc, 0, 0, 1, 1);

        JPanel locationsButtonsPanel = createLocationsButtonsPanel();
        addComponentToPanel(locationsButtonsPanel, locationsPanel, gbc, 0, 1, 1, 0);
        return locationsPanel;
    }

    private JPanel createLocationsButtonsPanel() {
        JPanel locationsButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addJarsButton = new JButton("Add JARs...");
        addJarsButton.addActionListener(e -> addJars());
        JButton addFolderButton = new JButton("Add Folder...");
        addFolderButton.addActionListener(e -> addFolder());
        JButton removeLocationsButton = new JButton("Remove location...");
        removeLocationsButton.addActionListener(e -> removeSelectedLocation());
        locationsButtonsPanel.add(addJarsButton);
        locationsButtonsPanel.add(addFolderButton);
        locationsButtonsPanel.add(removeLocationsButton);
        return locationsButtonsPanel;
    }

    private void addJars() {
        JFileChooser fileChooser = UITools.newFileChooser(null);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JAR Files", "jar");
        fileChooser.setFileFilter(filter);
        int option = fileChooser.showOpenDialog(CodeExplorerConfigurator.this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            int selectedConfigRow = getSelectedConfigurationIndex();
            if (selectedConfigRow >= 0) {
                CodeExplorerConfiguration selectedConfig = getConfiguration(selectedConfigRow);
                for (File file : files) {
                    locationsModel.addRow(new Object[]{file.getAbsolutePath()});
                    selectedConfig.getLocations().add(file);
                }
            }
        }
    }

    private void addFolder() {
        JFileChooser fileChooser = UITools.newFileChooser(null);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = fileChooser.showOpenDialog(CodeExplorerConfigurator.this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File folder = fileChooser.getSelectedFile();
            int selectedConfigRow = getSelectedConfigurationIndex();
            if (selectedConfigRow >= 0) {
                CodeExplorerConfiguration selectedConfig = getConfiguration(selectedConfigRow);
                locationsModel.addRow(new Object[]{folder.getAbsolutePath()});
                selectedConfig.getLocations().add(folder);
            }
        }
    }

    private void removeSelectedLocation() {
        int selectedIndex = locationTable.getSelectedRow();
        if (selectedIndex != -1) {
            locationsModel.removeRow(selectedIndex);
            int selectedConfigRow = getSelectedConfigurationIndex();
            if (selectedConfigRow >= 0) {
                CodeExplorerConfiguration config = getConfiguration(selectedConfigRow);
                config.getLocations().remove(selectedIndex);
            }
        }
    }
    private void layoutPanels() {
        setLayout(new GridLayout(1, 2));
        add(createConfigurationsPanel());
        add(createLocationsPanel());
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
        for (int i = 0; i < locationsModel.getRowCount(); i++) {
            paths.add(new File(locationsModel.getValueAt(i, 0).toString()));
        }
        return paths;
    }
}
