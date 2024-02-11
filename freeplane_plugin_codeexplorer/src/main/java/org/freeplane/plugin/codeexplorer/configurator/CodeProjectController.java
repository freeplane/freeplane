/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.plugin.codeexplorer.configurator;

import java.awt.Graphics2D;
import java.util.stream.Stream;

import javax.swing.JTabbedPane;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.SetBooleanPropertyAction;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.highlight.HighlightController;
import org.freeplane.features.highlight.NodeHighlighter;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.codeexplorer.archunit.ArchUnitServer;
import org.freeplane.plugin.codeexplorer.archunit.TestResultConfiguration;
import org.freeplane.plugin.codeexplorer.dependencies.CodeDependency;
import org.freeplane.plugin.codeexplorer.map.DependencySelection;
import org.freeplane.plugin.codeexplorer.task.CodeExplorer;
import org.freeplane.plugin.codeexplorer.task.UserDefinedCodeExplorerConfiguration;
import org.freeplane.plugin.codeexplorer.task.CodeExplorerConfigurations;
import org.freeplane.plugin.codeexplorer.task.CodeExplorerConfiguration;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.freeplane.extension.ArchTestResult;

/**
 * @author Dimitry Polivaev
 */
public class CodeProjectController implements IExtension {
    private CodeDependenciesPanel codeDependenciesPanel;
    private ModeController modeController;
    private JTabbedPane informationPanel;
    private CodeDependency selectedDependency;
    private CodeExplorerConfigurator configurator;
    private CodeExplorerConfigurations explorerConfigurations;
    private final ArchUnitServer archUnitServer;
    private TestResultPanel testResultPanel;
    /**
	 * @param modeController
	 */
	public CodeProjectController(ModeController modeController, ArchUnitServer archUnitServer) {
		super();
        this.modeController = modeController;
        this.archUnitServer = archUnitServer;
        this.explorerConfigurations = CodeExplorerConfigurations.loadConfigurations();

        Controller controller = modeController.getController();
        controller.getExtension(HighlightController.class).addNodeHighlighter(new NodeHighlighter() {
            @Override
            public boolean isNodeHighlighted(NodeModel node, boolean isPrinting) {
                return !isPrinting
                        && modeController == Controller.getCurrentModeController()
                        && isDependencySelectedForNode(node);
            }

            @Override
            public void configure(NodeModel node, Graphics2D g, boolean isPrinting) {
                g.setColor(FilterController.HIGHLIGHT_COLOR);
            }
        });
        modeController.addAction(new RunAnalysisAction(this));
        modeController.addAction(new SetBooleanPropertyAction(ArchUnitServer.ARCHUNIT_SERVER_ENABLED_PROPERTY));

	}


	private void showControlPanel() {
	    informationPanel = new JTabbedPane();

        configurator = new CodeExplorerConfigurator(this);
        informationPanel.addTab(TextUtils.getText("code.configurations"), configurator);

        final AFreeplaneAction enableServerAction = modeController.getAction(SetBooleanPropertyAction.actionKey(ArchUnitServer.ARCHUNIT_SERVER_ENABLED_PROPERTY));
        testResultPanel = new TestResultPanel (this, archUnitServer, enableServerAction);
        informationPanel.addTab(TextUtils.getText("code.testResults"), testResultPanel);

	    codeDependenciesPanel = new CodeDependenciesPanel();
	    codeDependenciesPanel.addDependencySelectionCallback(this::updateSelectedDependency);
        informationPanel.addTab(TextUtils.getText("code.dependencies"), codeDependenciesPanel);

	    modeController.getController().getViewController().insertComponentIntoSplitPane(informationPanel);
	    informationPanel.setVisible(true);
	    informationPanel.revalidate();
	}

    private void hideControlPanel() {
        modeController.getController().getViewController().removeSplitPane();
        informationPanel = null;
        configurator = null;
        codeDependenciesPanel = null;
        selectedDependency = null;
    }

    public void startupController() {
        showControlPanel();
        MapController mapController = modeController.getMapController();
        mapController.addNodeSelectionListener(codeDependenciesPanel);
        mapController.addMapChangeListener(codeDependenciesPanel);
        Controller.getCurrentController().getMapViewManager().addMapSelectionListener(codeDependenciesPanel);
        ResourceController.getResourceController().addPropertyChangeListener(codeDependenciesPanel);
        codeDependenciesPanel.update();
    }

	public void shutdownController() {
	    MapController mapController = modeController.getMapController();
        mapController.removeNodeSelectionListener(codeDependenciesPanel);
        mapController.removeMapChangeListener(codeDependenciesPanel);
	    Controller.getCurrentController().getMapViewManager().removeMapSelectionListener(codeDependenciesPanel);
	    ResourceController.getResourceController().removePropertyChangeListener(codeDependenciesPanel);
	    hideControlPanel();
	}

    private boolean isDependencySelectedForNode(NodeModel node) {
        if(selectedDependency == null)
            return false;
        IMapSelection selection = Controller.getCurrentController().getSelection();
        if(selection == null || node.getMap() != selection.getMap())
            return false;
        DependencySelection dependencySelection = new DependencySelection(selection);
        JavaClass[] dependencyClasses =  { selectedDependency.getOriginClass(), selectedDependency.getTargetClass()};
        return Stream.of(dependencyClasses)
                .anyMatch(javaClass -> node.equals(dependencySelection.getVisibleNode(javaClass)));
    }

    private void updateSelectedDependency(CodeDependency selectedDependency) {
        this.selectedDependency = selectedDependency;
        modeController.getController().getMapViewManager().getMapViewComponent().repaint();
    }

    void exploreSelectedConfiguration() {
        if(configurator != null) {
            UserDefinedCodeExplorerConfiguration selectedConfiguration = configurator.getSelectedConfiguration();
            if(selectedConfiguration != null)
                exploreConfiguration(selectedConfiguration);
        }
    }


    public void exploreConfiguration(ArchTestResult testResult) {
        CodeExplorer codeExplorer = (CodeExplorer) Controller.getCurrentModeController().getMapController();
        codeExplorer.explore(configuration(testResult));
    }

    private CodeExplorerConfiguration configuration(ArchTestResult testResult) {
        return new TestResultConfiguration(testResult);
    }


    void exploreConfiguration(UserDefinedCodeExplorerConfiguration selectedConfiguration) {
        CodeExplorer codeExplorer = (CodeExplorer) Controller.getCurrentModeController().getMapController();
        codeExplorer.explore(selectedConfiguration);
    }

    public void saveConfiguration() {
        explorerConfigurations.saveConfiguration();
    }

    public CodeExplorerConfigurations explorerConfigurations() {
        return explorerConfigurations;
    }

    public void updateProjectConfiguration() {
        if(configurator != null) {
            UserDefinedCodeExplorerConfiguration selectedConfiguration = configurator.getSelectedConfiguration();
            CodeExplorer codeExplorer = (CodeExplorer) Controller.getCurrentModeController().getMapController();
            codeExplorer.setProjectConfiguration(selectedConfiguration.getDependencyJudge(), selectedConfiguration.getAnnotationMatcher());
            codeDependenciesPanel.update();
        }
    }

    public void cancelAnalysis() {
        CodeExplorer codeExplorer = (CodeExplorer) Controller.getCurrentModeController().getMapController();
        codeExplorer.cancelAnalysis();
    }
}
