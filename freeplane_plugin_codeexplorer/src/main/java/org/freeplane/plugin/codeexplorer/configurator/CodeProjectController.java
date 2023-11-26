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
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.highlight.HighlightController;
import org.freeplane.features.highlight.NodeHighlighter;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.codeexplorer.map.DependencySelection;

import com.tngtech.archunit.core.domain.JavaClass;

/**
 * @author Dimitry Polivaev
 */
public class CodeProjectController implements IExtension {
    private CodeDependenciesPanel codeDependenciesPanel;
    private ModeController modeController;
    private JTabbedPane informationPanel;
    private CodeDependency selectedDependency;
    /**
	 * @param modeController
	 */
	public CodeProjectController(ModeController modeController) {
		super();
        this.modeController = modeController;
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

	}

	private void hideControlPanel() {
		modeController.getController().getViewController().removeSplitPane();
	}


	private void showControlPanel() {
	    informationPanel = new JTabbedPane();
	    codeDependenciesPanel = new CodeDependenciesPanel();
	    codeDependenciesPanel.addDependencySelectionCallback(this::updateSelectedDependency);

        informationPanel.addTab("Configurations", new CodeExplorerConfigurator());
        informationPanel.addTab("Dependencies", codeDependenciesPanel);

	    modeController.getController().getViewController().insertComponentIntoSplitPane(informationPanel);
	    informationPanel.setVisible(true);
	    informationPanel.revalidate();
	}

    public void startupController() {
        showControlPanel();
        modeController.getMapController().addNodeSelectionListener(codeDependenciesPanel);
        Controller.getCurrentController().getMapViewManager().addMapSelectionListener(codeDependenciesPanel);
        ResourceController.getResourceController().addPropertyChangeListener(codeDependenciesPanel);
        codeDependenciesPanel.update();
    }

	public void shutdownController() {
	    modeController.getMapController().removeNodeSelectionListener(codeDependenciesPanel);
	    Controller.getCurrentController().getMapViewManager().removeMapSelectionListener(codeDependenciesPanel);
	    ResourceController.getResourceController().removePropertyChangeListener(codeDependenciesPanel);
	    hideControlPanel();
	    informationPanel = null;
	    codeDependenciesPanel = null;
	    selectedDependency = null;
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
                .anyMatch(javaClass -> node.getID().equals(dependencySelection.getVisibleNodeId(javaClass)));
    }

    private void updateSelectedDependency(CodeDependency selectedDependency) {
        this.selectedDependency = selectedDependency;
        modeController.getController().getMapViewManager().getMapViewComponent().repaint();
    }



}
