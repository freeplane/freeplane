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
package org.freeplane.features.clipboard.mindmapmode;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.clipboard.ClipboardController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

class CutAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CutAction() {
		super("CutAction");
	}

	public void actionPerformed(final ActionEvent e) {
		final ModeController mMindMapController = Controller.getCurrentModeController();
		final Controller controller = Controller.getCurrentController();
		final NodeModel root = controller.getMap().getRootNode();
		if (controller.getSelection().isSelected(root)) {
			UITools.errorMessage(TextUtils.getText("cannot_delete_root"));
			return;
		}
		final int showResult = OptionalDontShowMeAgainDialog.show("really_cut_node", "confirmation",
		    MClipboardController.RESOURCES_CUT_NODES_WITHOUT_QUESTION,
		    OptionalDontShowMeAgainDialog.ONLY_OK_SELECTION_IS_STORED);
		if (showResult != JOptionPane.OK_OPTION) {
			return;
		}
		final MClipboardController clipboardController = (MClipboardController) mMindMapController
		    .getExtension(ClipboardController.class);
		clipboardController.cut(controller.getSelection().getSortedSelection(true));
		controller.getMapViewManager().obtainFocusForSelected();
	}
}
