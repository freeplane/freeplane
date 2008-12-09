/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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

import org.freeplane.controller.Controller;
import org.freeplane.map.nodelocation.LocationController;
import org.freeplane.map.nodelocation.LocationModel;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.mindmapmode.MModeController;
import org.freeplane.undo.IUndoableActor;

/**
 * @author Dimitry Polivaev
 */
public class MLocationController extends LocationController {
	private static final class ChangeNodePositionActor implements
	        IUndoableActor {
		private final int gap;
		private final NodeModel node;
		private final int oldHgap;
		private final int oldParentVgap;
		private final int oldShiftY;
		private final int parentVGap;
		private final int shiftY;

		private ChangeNodePositionActor(final NodeModel node, final int gap,
		                                final int shiftY, final int parentVGap) {
			this.node = node;
			this.gap = gap;
			this.shiftY = shiftY;
			this.parentVGap = parentVGap;
			final LocationModel locationModel = node.getLocationModel();
			oldHgap = locationModel.getHGap();
			oldShiftY = locationModel.getShiftY();
			oldParentVgap = !node.isRoot() ? node.getParentNode()
			    .getLocationModel().getVGap() : 0;
		}

		public void act() {
			moveNodePosition(node, parentVGap, gap, shiftY);
		}

		public String getDescription() {
			return "moveNodePosition";
		}

		private void moveNodePosition(final NodeModel node,
		                              final int parentVGap, final int hGap,
		                              final int shiftY) {
			final LocationModel locationModel = node.createLocationModel();
			locationModel.setHGap(hGap);
			locationModel.setShiftY(shiftY);
			if (!node.isRoot()) {
				node.getParentNode().createLocationModel().setVGap(parentVGap);
			}
			node.getModeController().getMapController().nodeChanged(node);
		}

		public void undo() {
			moveNodePosition(node, oldParentVgap, oldHgap, oldShiftY);
		}
	}

	private static boolean actionsCreated = false;

	public MLocationController(final MModeController modeController) {
		super(modeController);
		modeController.setNodeMotionListener(new MNodeMotionListener(
		    modeController));
		createActions(modeController);
	}

	private void createActions(final MModeController modeController) {
		if (actionsCreated == false) {
			actionsCreated = true;
			Controller.getController().addAction("moveNodeAction",
			    new ResetNodeLocationAction());
		}
	}

	public MModeController getMModeController() {
		return (MModeController) getModeController();
	}

	public void moveNodePosition(final NodeModel node, final int parentVGap,
	                             final int hGap, final int shiftY) {
		final IUndoableActor actor = new ChangeNodePositionActor(node, hGap,
		    shiftY, parentVGap);
		getModeController().execute(actor);
	}
}
