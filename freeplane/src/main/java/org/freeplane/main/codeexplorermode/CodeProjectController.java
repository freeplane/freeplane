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
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 */
class CodeProjectController implements IExtension {

    public static CodeProjectController getController() {
	    return (CodeProjectController) CodeProjectController.getController();
	}

    private CodeInformationPanel informationPanel;
    private CodeModeController modeController;
    /**
	 * @param modeController
	 */
	public CodeProjectController(CodeModeController modeController) {
		super();
        this.modeController = modeController;
	}

	void hideControlPanel() {
		modeController.getController().getViewController().removeSplitPane();
	}


	void showControlPanel() {
		if (informationPanel == null) {
			informationPanel = new CodeInformationPanel();
		}
		modeController.getController().getViewController().insertComponentIntoSplitPane(informationPanel);
		informationPanel.setVisible(true);
		informationPanel.revalidate();
	}

	public void shutdownController() {
	    if(informationPanel != null) {
	        modeController.getMapController().removeNodeSelectionListener(informationPanel);
	        Controller.getCurrentController().getMapViewManager().removeMapSelectionListener(informationPanel);
	    }
		if (informationPanel == null) {
		    return;
		}
		hideControlPanel();
		informationPanel = null;
	}

	public void startupController() {
	    showControlPanel();
	    if(informationPanel != null) {
	        modeController.getMapController().addNodeSelectionListener(informationPanel);
	        Controller.getCurrentController().getMapViewManager().addMapSelectionListener(informationPanel);
	        informationPanel.update();
	    }
	}


}
