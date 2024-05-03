/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.plugin.codeexplorer.map;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog.MessageType;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;

class PurgeAction extends AFreeplaneAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public PurgeAction() {
		super("code.PurgeAction");
	}

	@Override
    public void actionPerformed(final ActionEvent e) {
		final int showResult = OptionalDontShowMeAgainDialog.show("really_remove_node",
		    "code.really_remove_node",
		    MessageType.ONLY_OK_SELECTION_IS_STORED);
		if (showResult != JOptionPane.OK_OPTION) {
			return;
		}
		final ModeController modeController = Controller.getCurrentModeController();
		final CodeMapController mapController = (CodeMapController) modeController.getMapController();
		final Controller controller = Controller.getCurrentController();
		new ArrayList<>(controller.getSelection().getSortedSelection(true)).forEach(mapController::purge);
		controller.getMapViewManager().obtainFocusForSelected();
	}
}
