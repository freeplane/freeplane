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
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.core.undo.IActor;

class DeleteAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DeleteAction(final Controller controller) {
		super("DeleteAction", controller);
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
		final int showResult = OptionalDontShowMeAgainDialog.show(controller, "really_remove_node", "confirmation",
		    MModeController.RESOURCES_DELETE_NODES_WITHOUT_QUESTION,
		    OptionalDontShowMeAgainDialog.ONLY_OK_SELECTION_IS_STORED);
		if (showResult != JOptionPane.OK_OPTION) {
			return;
		}
		final Iterator<NodeModel> iterator = controller.getSelection().getSortedSelection(true).iterator();
		while (iterator.hasNext()) {
			delete(iterator.next());
		}
	}

	public IActor createActor(final int index, final NodeModel parentNode, final NodeModel node) {
		final IActor actor = new IActor() {
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
		final IActor actor = createActor(index, parentNode, node);
		getModeController().execute(actor, node.getMap());
	}

	void deleteWithoutUndo(final NodeModel node) {
		((MMapController) getModeController().getMapController()).deleteWithoutUndo(node);
	}
}
