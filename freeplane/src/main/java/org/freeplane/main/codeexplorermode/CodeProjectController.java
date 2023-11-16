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

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 */
class CodeProjectController implements IExtension {
    private CodeInformationPanel informationPanel;
    private CodeModeController modeController;
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
	    informationPanel = new CodeInformationPanel();
	    modeController.getController().getViewController().insertComponentIntoSplitPane(informationPanel);
	    informationPanel.setVisible(true);
	    informationPanel.revalidate();
	}

	public void shutdownController() {
	    modeController.getMapController().removeNodeSelectionListener(informationPanel);
	    Controller.getCurrentController().getMapViewManager().removeMapSelectionListener(informationPanel);
	    ResourceController.getResourceController().removePropertyChangeListener(informationPanel);
	    hideControlPanel();
	    informationPanel = null;
	}

	public void startupController() {
	    showControlPanel();
	    modeController.getMapController().addNodeSelectionListener(informationPanel);
	    Controller.getCurrentController().getMapViewManager().addMapSelectionListener(informationPanel);
	    ResourceController.getResourceController().addPropertyChangeListener(informationPanel);
	    informationPanel.update();
	}


}
