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
package org.freeplane.map.clipboard.mindmapmode;

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.map.ModeController;
import org.freeplane.core.map.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.FreeplaneAction;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.features.mindmapmode.MMapController;
import org.freeplane.map.clipboard.ClipboardController;

class CutAction extends FreeplaneAction {
	public CutAction() {
		super("cut", "images/editcut.png");
	}

	public void actionPerformed(final ActionEvent e) {
		final ModeController mMindMapController = getModeController();
		if (Controller.getController().getMapView().getRoot().isSelected()) {
			Controller.getController().errorMessage(Controller.getText("cannot_delete_root"));
			return;
		}
		final int showResult = new OptionalDontShowMeAgainDialog(Controller.getController()
		    .getViewController().getJFrame(), mMindMapController.getMapController()
		    .getSelectedView(), "really_cut_node", "confirmation",
		    new OptionalDontShowMeAgainDialog.StandardPropertyHandler(
		        ResourceController.RESOURCES_CUT_NODES_WITHOUT_QUESTION),
		    OptionalDontShowMeAgainDialog.ONLY_OK_SELECTION_IS_STORED).show().getResult();
		if (showResult != JOptionPane.OK_OPTION) {
			return;
		}
		final Transferable copy = cut(Controller.getController().getMapView()
		    .getSelectedNodesSortedByY());
		ClipboardController.getController(mMindMapController).setClipboardContents(copy);
		Controller.getController().getViewController().obtainFocusForSelected();
	}

	Transferable cut(final List<NodeModel> collection) {
		getModeController().getMapController().sortNodesByDepth(collection);
		final Transferable totalCopy = ClipboardController.getController(getModeController()).copy(
		    collection, true);
		for (final Iterator i = collection.iterator(); i.hasNext();) {
			final NodeModel node = (NodeModel) i.next();
			if (node.getParentNode() != null) {
				((MMapController) getModeController().getMapController()).deleteNode(node);
			}
		}
		return totalCopy;
	}
}
