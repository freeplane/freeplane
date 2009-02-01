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
package org.freeplane.features.mindmapmode;

import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.FreeplaneAction;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.core.undo.IUndoableActor;

class DeleteAction extends FreeplaneAction {
	public DeleteAction(final Controller controller) {
		super(controller, "remove_node", "/images/editdelete.png");
	}

	public void actionPerformed(final ActionEvent e) {
		final ModeController modeController = getModeController();
		for (final Iterator iterator = modeController.getMapController().getSelectedNodes().iterator(); iterator
		    .hasNext();) {
			final NodeModel node = (NodeModel) iterator.next();
			if (node.isRoot()) {
				return;
			}
		}
		final Controller controller = getController();
		final ViewController viewController = controller.getViewController();
		final int showResult = new OptionalDontShowMeAgainDialog(viewController.getJFrame(), controller.getSelection()
		    .getSelected(), "really_remove_node", "confirmation",
		    new OptionalDontShowMeAgainDialog.StandardPropertyHandler(
		        ResourceController.RESOURCES_DELETE_NODES_WITHOUT_QUESTION),
		    OptionalDontShowMeAgainDialog.ONLY_OK_SELECTION_IS_STORED).show().getResult();
		if (showResult != JOptionPane.OK_OPTION) {
			return;
		}
		final Iterator<NodeModel> iterator = controller.getSelection().getSortedSelection().iterator();
		while (iterator.hasNext()) {
			delete(iterator.next());
		}
	}

	public IUndoableActor createActor(final int index, final NodeModel parentNode, final NodeModel node) {
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				deleteWithoutUndo(node);
			}

			public String getDescription() {
				return "delete";
			}

			public void undo() {
				(getModeController().getMapController()).insertNodeIntoWithoutUndo(node, parentNode, index);
			}
		};
		return actor;
	}

	/**
	 * @param node
	 */
	void delete(final NodeModel node) {
		final NodeModel parentNode = node.getParentNode();
		final int index = parentNode.getIndex(node);
		final IUndoableActor actor = createActor(index, parentNode, node);
		getModeController().execute(actor);
	}

	void deleteWithoutUndo(final NodeModel node) {
		((MMapController) getModeController().getMapController()).deleteWithoutUndo(node);
	}
}
