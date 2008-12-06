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
import java.awt.event.KeyEvent;

import org.freeplane.controller.Freeplane;
import org.freeplane.map.text.mindmapmode.MTextController;
import org.freeplane.map.tree.MapController;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.view.NodeView;
import org.freeplane.modes.ModeController;
import org.freeplane.modes.ModeControllerAction;
import org.freeplane.modes.mindmapmode.MModeController;
import org.freeplane.undo.IUndoableActor;

class NewChildAction extends ModeControllerAction {
	public NewChildAction(final MModeController modeController) {
		super(modeController, "new_child", "images/idea.png");
	}

	public void actionPerformed(final ActionEvent e) {
		((MMapController) getModeController().getMapController()).addNewNode(
		    MMapController.NEW_CHILD, null);
	}

	public NodeModel addNewNode(int newNodeMode, final KeyEvent e) {
		final ModeController modeController = getModeController();
		final NodeModel target = modeController.getSelectedNode();
		((MTextController) modeController.getTextController()).stopEditing();
		final NodeModel targetNode = target;
		NodeModel newNode = null;
		switch (newNodeMode) {
			case MMapController.NEW_SIBLING_BEFORE:
			case MMapController.NEW_SIBLING_BEHIND: {
				if (!targetNode.isRoot()) {
					final NodeModel parent = targetNode.getParentNode();
					int childPosition = parent.getChildPosition(targetNode);
					if (newNodeMode == MMapController.NEW_SIBLING_BEHIND) {
						childPosition++;
					}
					newNode = addNewNode(parent, childPosition, targetNode
					    .isLeft());
					final NodeView nodeView = modeController
					    .getNodeView(newNode);
					modeController.select(nodeView);
					((MTextController) modeController.getTextController())
					    .edit(nodeView, modeController.getNodeView(target), e,
					        true, false, false);
					break;
				}
				else {
					newNodeMode = MMapController.NEW_CHILD;
				}
			}
			case MMapController.NEW_CHILD:
			case MMapController.NEW_CHILD_WITHOUT_FOCUS: {
				final MapController mapController = modeController
				    .getMapController();
				final boolean parentFolded = mapController.isFolded(targetNode);
				if (parentFolded) {
					mapController.setFolded(targetNode, false);
				}
				final int position = Freeplane.getController()
				    .getResourceController().getProperty("placenewbranches")
				    .equals("last") ? targetNode.getChildCount() : 0;
				newNode = addNewNode(targetNode, position, targetNode
				    .isNewChildLeft());
				final NodeView nodeView = modeController.getNodeView(newNode);
				if (newNodeMode == MMapController.NEW_CHILD) {
					modeController.select(nodeView);
				}
				((MTextController) modeController.getTextController()).edit(
				    nodeView, modeController.getNodeView(target), e, true,
				    parentFolded, false);
				break;
			}
		}
		return newNode;
	}

	public NodeModel addNewNode(final NodeModel parent, final int index,
	                            final boolean newNodeIsLeft) {
		final NodeModel newNode = getMModeController().getMapController()
		    .newNode("", parent.getMap());
		newNode.setLeft(newNodeIsLeft);
		getModeController().getMapController().insertNodeIntoWithoutUndo(
		    newNode, parent, index);
		addUndoAction(newNode);
		return newNode;
	}

	private void addUndoAction(final NodeModel node) {
		final NodeModel parentNode = node.getParentNode();
		final int index = parentNode.getIndex(node);
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				((MMapController) getModeController().getMapController())
				    .insertNodeIntoWithoutUndo(node, parentNode, index);
			}

			public String getDescription() {
				return "paste";
			}

			public void undo() {
				((MMapController) getModeController().getMapController())
				    .deleteWithoutUndo(node);
			}
		};
		getMModeController().addUndoableActor(actor);
	}
}
