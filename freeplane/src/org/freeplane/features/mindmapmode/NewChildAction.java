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

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.mindmapmode.text.MTextController;

class NewChildAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NewChildAction(final Controller controller) {
		super("NewChildAction", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		addNewNode(MMapController.NEW_CHILD, null);
	}

	public NodeModel addNewNode(int newNodeMode, final KeyEvent e) {
		final ModeController modeController = getModeController();
		final MMapController mapController = (MMapController) modeController.getMapController();
		final NodeModel target = mapController.getSelectedNode();
		modeController.startTransaction();
		try{
			((MTextController) TextController.getController(modeController)).stopEditing();
		}
		finally{
			modeController.commit();
		}
		final NodeModel targetNode = target;
		final NodeModel newNode;
		switch (newNodeMode) {
			case MMapController.NEW_SIBLING_BEFORE:
			case MMapController.NEW_SIBLING_BEHIND: {
				if (!targetNode.isRoot()) {
					final NodeModel parent = targetNode.getParentNode();
					int childPosition = parent.getChildPosition(targetNode);
					if (newNodeMode == MMapController.NEW_SIBLING_BEHIND) {
						childPosition++;
					}
					newNode = addNewNode(parent, childPosition, targetNode.isLeft());
					if(newNode == null){
						return null;
					}
					mapController.select(newNode);
					if (e != null) {
						((MTextController) TextController.getController(modeController)).edit(newNode, targetNode, e,
						    true, false, false);
					}
					else {
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								((MTextController) TextController.getController(modeController)).edit(newNode,
								    targetNode, e, true, false, false);
							}
						});
					}
					break;
				}
				else {
					newNodeMode = MMapController.NEW_CHILD;
				}
			}
			case MMapController.NEW_CHILD:
			case MMapController.NEW_CHILD_WITHOUT_FOCUS: {
				final boolean parentFolded = mapController.isFolded(targetNode);
				if (parentFolded) {
					mapController.setFolded(targetNode, false);
				}
				final int position = ResourceController.getResourceController().getProperty("placenewbranches").equals(
				    "last") ? targetNode.getChildCount() : 0;
				newNode = addNewNode(targetNode, position, targetNode.isNewChildLeft());
				if(newNode == null){
					return null;
				}
				if (newNodeMode == MMapController.NEW_CHILD) {
					mapController.select(newNode);
				}
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						((MTextController) TextController.getController(modeController)).edit(newNode, targetNode, e,
						    true, parentFolded, false);
					}
				});
				break;
			}
			default:
				newNode = null;
		}
		return newNode;
	}

	public NodeModel addNewNode(final NodeModel parent, final int index, final boolean newNodeIsLeft) {
		final MMapController mapController = (MMapController) getModeController().getMapController();
		if (! mapController.isWriteable(parent)) {
			final String message = ResourceBundles.getText("node_is_write_protected");
			UITools.errorMessage(message);
			return null;
		}
		final MapModel map = parent.getMap();
		final NodeModel newNode = getModeController().getMapController().newNode("", map);
		newNode.setLeft(newNodeIsLeft);
		final IActor actor = new IActor() {
			public void act() {
				(getModeController().getMapController()).insertNodeIntoWithoutUndo(newNode, parent, index);
			}

			public String getDescription() {
				return "addNewNode";
			}

			public void undo() {
				mapController.deleteWithoutUndo(newNode);
			}
		};
		getModeController().execute(actor, map);
		return newNode;
	}
}
