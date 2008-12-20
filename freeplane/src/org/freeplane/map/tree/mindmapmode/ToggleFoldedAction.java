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
package org.freeplane.map.tree.mindmapmode;

import java.awt.event.ActionEvent;
import java.util.ListIterator;

import org.freeplane.controller.Controller;
import org.freeplane.controller.FreeplaneAction;
import org.freeplane.controller.resources.ResourceController;
import org.freeplane.main.Tools;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.ModeController;
import org.freeplane.undo.IUndoableActor;

class ToggleFoldedAction extends FreeplaneAction {
	public ToggleFoldedAction() {
		super("toggle_folded");
	}

	public void actionPerformed(final ActionEvent e) {
		toggleFolded();
	}

	/**
	 */
	public void setFolded(final NodeModel node, final boolean folded) {
		if (node.isFolded() == folded) {
			return;
		}
		toggleFolded(node);
	}

	public void toggleFolded() {
		toggleFolded(getModeController().getSelectedNodes().listIterator());
	}

	public void toggleFolded(final ListIterator listIterator) {
		while (listIterator.hasNext()) {
			toggleFolded((NodeModel) listIterator.next());
		}
	}

	private void toggleFolded(final NodeModel node) {
		if (!node.getModeController().getMapController().hasChildren(node)
		        && !Tools.safeEquals(Controller.getResourceController().getProperty(
		            "enable_leaves_folding"), "true")) {
			return;
		}
		final ModeController modeController = getModeController();
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				modeController.getMapController()._setFolded(node, !node.isFolded());
				if (Controller.getResourceController().getBoolProperty(
				    ResourceController.RESOURCES_SAVE_FOLDING_STATE)) {
					modeController.getMapController().nodeChanged(node);
				}
			}

			public String getDescription() {
				return "toggleFolded";
			}

			public void undo() {
				act();
			}
		};
		modeController.execute(actor);
	}
}
