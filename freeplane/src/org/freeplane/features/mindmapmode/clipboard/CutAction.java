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
package org.freeplane.features.mindmapmode.clipboard;

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.enums.ResourceControllerProperties;
import org.freeplane.core.extension.ExtensionContainer;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.features.common.clipboard.ClipboardController;
import org.freeplane.features.mindmapmode.MMapController;

class CutAction extends AFreeplaneAction {
	public CutAction(final Controller controller) {
		super(controller, "cut", "/images/editcut.png");
	}

	public void actionPerformed(final ActionEvent e) {
		final ExtensionContainer mMindMapController = getModeController();
		final Controller controller = getController();
		final NodeModel root = controller.getMap().getRootNode();
		if (controller.getSelection().isSelected(root)) {
			controller.errorMessage(ResourceController.getText("cannot_delete_root"));
			return;
		}
		final int showResult = new OptionalDontShowMeAgainDialog(controller.getViewController().getFrame(), controller
		    .getSelection().getSelected(), "really_cut_node", "confirmation",
		    new OptionalDontShowMeAgainDialog.StandardPropertyHandler(
		        ResourceControllerProperties.RESOURCES_CUT_NODES_WITHOUT_QUESTION),
		    OptionalDontShowMeAgainDialog.ONLY_OK_SELECTION_IS_STORED).show().getResult();
		if (showResult != JOptionPane.OK_OPTION) {
			return;
		}
		final Transferable copy = cut(controller.getSelection().getSortedSelection());
		((ClipboardController)mMindMapController.getExtension(ClipboardController.class)).setClipboardContents(copy);
		controller.getViewController().obtainFocusForSelected();
	}

	Transferable cut(final List<NodeModel> collection) {
		getModeController().getMapController().sortNodesByDepth(collection);
		final Transferable totalCopy = ((ClipboardController)getModeController().getExtension(ClipboardController.class)).copy(collection, true);
		for (final Iterator i = collection.iterator(); i.hasNext();) {
			final NodeModel node = (NodeModel) i.next();
			if (node.getParentNode() != null) {
				((MMapController) getModeController().getMapController()).deleteNode(node);
			}
		}
		return totalCopy;
	}
}
