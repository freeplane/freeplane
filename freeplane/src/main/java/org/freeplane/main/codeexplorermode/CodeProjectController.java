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
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

/**
 * @author Dimitry Polivaev
 */
class CodeProjectController implements IExtension, INodeSelectionListener, IMapSelectionListener {

    public static CodeProjectController getController() {
	    return (CodeProjectController) CodeProjectController.getController();
	}

    private CodeInformationPanel informationPanel;
    /**
	 * @param modeController
	 */
	public CodeProjectController(CodeModeController modeController) {
		super();
	}

	void hideControlPanel() {
		Controller.getCurrentModeController().getController().getViewController().removeSplitPane();
	}


	void showControlPanel() {
		if (informationPanel == null) {
			informationPanel = new CodeInformationPanel();
		}
		Controller.getCurrentModeController().getController().getViewController().insertComponentIntoSplitPane(informationPanel);
		informationPanel.setVisible(true);
		informationPanel.revalidate();
	}

	public void shutdownController() {
		Controller.getCurrentModeController().getMapController().removeNodeSelectionListener(this);
		Controller.getCurrentController().getMapViewManager().removeMapSelectionListener(this);
		if (informationPanel == null) {
		    return;
		}
		hideControlPanel();
		informationPanel = null;
	}

	public void startupController() {
		final ModeController modeController = Controller.getCurrentModeController();
			showControlPanel();
		modeController.getMapController().addNodeSelectionListener(this);
		Controller.getCurrentController().getMapViewManager().addMapSelectionListener(this);
	}

}
