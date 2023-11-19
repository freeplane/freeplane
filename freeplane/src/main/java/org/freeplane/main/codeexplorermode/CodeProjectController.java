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
package org.freeplane.main.codeexplorermode;

import javax.swing.JTabbedPane;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 */
class CodeProjectController implements IExtension {
    private CodeDependenciesPanel codeDependenciesPanel;
    private CodeModeController modeController;
    private JTabbedPane informationPanel;
    /**
	 * @param modeController
	 */
	public CodeProjectController(CodeModeController modeController) {
		super();
        this.modeController = modeController;
	}

	private void hideControlPanel() {
		modeController.getController().getViewController().removeSplitPane();
	}


	private void showControlPanel() {
	    informationPanel = new JTabbedPane();
	    codeDependenciesPanel = new CodeDependenciesPanel();
        informationPanel.addTab("Configurations", new CodeExplorerConfigurator());
        informationPanel.addTab("Dependencies", codeDependenciesPanel);

	    modeController.getController().getViewController().insertComponentIntoSplitPane(informationPanel);
	    informationPanel.setVisible(true);
	    informationPanel.revalidate();
	}

	public void shutdownController() {
	    modeController.getMapController().removeNodeSelectionListener(codeDependenciesPanel);
	    Controller.getCurrentController().getMapViewManager().removeMapSelectionListener(codeDependenciesPanel);
	    ResourceController.getResourceController().removePropertyChangeListener(codeDependenciesPanel);
	    hideControlPanel();
	    informationPanel = null;
	    codeDependenciesPanel = null;
	}

	public void startupController() {
	    showControlPanel();
	    modeController.getMapController().addNodeSelectionListener(codeDependenciesPanel);
	    Controller.getCurrentController().getMapViewManager().addMapSelectionListener(codeDependenciesPanel);
	    ResourceController.getResourceController().addPropertyChangeListener(codeDependenciesPanel);
	    codeDependenciesPanel.update();
	}


}
