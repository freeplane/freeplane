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
package org.freeplane.map.nodelocation.mindmapmode;

import javax.swing.Action;

import org.freeplane.map.nodelocation.LocationModel;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.mindmapmode.MModeController;

import deprecated.freemind.modes.mindmapmode.actions.instance.ActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.instance.MoveNodeActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.undo.ActionPair;
import deprecated.freemind.modes.mindmapmode.actions.undo.INodeActor;
import deprecated.freemind.modes.mindmapmode.actions.undo.NodeGeneralAction;

class MoveNodeAction extends NodeGeneralAction implements INodeActor {
	final private MModeController modeController;

	/**
	 */
	public MoveNodeAction(final MModeController modeController) {
		super(modeController, "reset_node_position", (String) null);
		this.modeController = modeController;
		addActor(this);
	}

	@Override
	public void act(final ActionInstance action) {
		final MoveNodeActionInstance moveAction = (MoveNodeActionInstance) action;
		final NodeModel node = getNodeFromID(moveAction.getNode());
		final LocationModel locationModel = node.createLocationModel();
		locationModel.setHGap(moveAction.getHGap());
		locationModel.setShiftY(moveAction.getShiftY());
		if (!node.isRoot()) {
			node.getParentNode().createLocationModel().setVGap(
			    moveAction.getVGap());
		}
		modeController.getMapController().nodeChanged(node);
	}

	public ActionPair getActionPair(final NodeModel selected) {
		if (selected.isRoot()) {
			return null;
		}
		return getActionPair(selected, LocationModel.VGAP, LocationModel.HGAP,
		    0);
	}

	private ActionPair getActionPair(final NodeModel selected,
	                                 final int parentVGap, final int hGap,
	                                 final int shiftY) {
		final MoveNodeActionInstance moveAction = moveNode(selected,
		    parentVGap, hGap, shiftY);
		final MoveNodeActionInstance undoAction = moveNode(selected, selected
		    .getParentNode().getLocationModel().getVGap(), selected
		    .getLocationModel().getHGap(), selected.getLocationModel()
		    .getShiftY());
		return new ActionPair(moveAction, undoAction);
	}

	public Class getDoActionClass() {
		return MoveNodeActionInstance.class;
	}

	private MoveNodeActionInstance moveNode(final NodeModel selected,
	                                        final int parentVGap,
	                                        final int hGap, final int shiftY) {
		final MoveNodeActionInstance moveNodeAction = new MoveNodeActionInstance();
		moveNodeAction.setNode(getNodeID(selected));
		moveNodeAction.setHGap(hGap);
		moveNodeAction.setVGap(parentVGap);
		moveNodeAction.setShiftY(shiftY);
		return moveNodeAction;
	}

	public void moveNodeTo(final NodeModel node, final int parentVGap,
	                       final int hGap, final int shiftY) {
		if (parentVGap == node.getParentNode().getLocationModel().getVGap()
		        && hGap == node.getLocationModel().getHGap()
		        && shiftY == node.getLocationModel().getShiftY()) {
			return;
		}
		modeController.getActionFactory().startTransaction(
		    (String) getValue(Action.NAME));
		modeController.getActionFactory().executeAction(
		    getActionPair(node, parentVGap, hGap, shiftY));
		modeController.getActionFactory().endTransaction(
		    (String) getValue(Action.NAME));
	}
}
