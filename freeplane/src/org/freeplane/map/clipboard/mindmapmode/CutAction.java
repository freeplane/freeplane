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

import org.freeplane.controller.Controller;
import org.freeplane.controller.FreeplaneAction;
import org.freeplane.controller.resources.ResourceController;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.mindmapmode.MMapController;
import org.freeplane.modes.mindmapmode.MModeController;
import org.freeplane.ui.dialogs.OptionalDontShowMeAgainDialog;


class CutAction extends FreeplaneAction {
	public CutAction() {
		super("cut", "images/editcut.png");
	}

	public void actionPerformed(final ActionEvent e) {
		final MModeController mMindMapController = getMModeController();
		if (mMindMapController.getMapView().getRoot().isSelected()) {
			Controller.getController().errorMessage(Controller.getText("cannot_delete_root"));
			return;
		}
		final int showResult = new OptionalDontShowMeAgainDialog(Controller.getController()
		    .getViewController().getJFrame(), mMindMapController.getSelectedView(),
		    "really_cut_node", "confirmation",
		    new OptionalDontShowMeAgainDialog.StandardPropertyHandler(
		        ResourceController.RESOURCES_CUT_NODES_WITHOUT_QUESTION),
		    OptionalDontShowMeAgainDialog.ONLY_OK_SELECTION_IS_STORED).show().getResult();
		if (showResult != JOptionPane.OK_OPTION) {
			return;
		}
		final Transferable copy = cut(mMindMapController.getMapView().getSelectedNodesSortedByY());
		mMindMapController.getClipboardController().setClipboardContents(copy);
		Controller.getController().getViewController().obtainFocusForSelected();
	}

	Transferable cut(final List nodeList) {
		getModeController().getMapController().sortNodesByDepth(nodeList);
		final Transferable totalCopy = getModeController().getClipboardController().copy(nodeList,
		    true);
		for (final Iterator i = nodeList.iterator(); i.hasNext();) {
			final NodeModel node = (NodeModel) i.next();
			if (node.getParentNode() != null) {
				((MMapController) getModeController().getMapController()).deleteNode(node);
			}
		}
		return totalCopy;
	}
}
