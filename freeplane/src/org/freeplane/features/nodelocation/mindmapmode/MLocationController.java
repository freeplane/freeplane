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
package org.freeplane.features.nodelocation.mindmapmode;

import org.freeplane.core.undo.IActor;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodelocation.LocationController;
import org.freeplane.features.nodelocation.LocationModel;

/**
 * @author Dimitry Polivaev
 */
public class MLocationController extends LocationController {
	private final class ChangeNodePositionActor implements IActor {
		private final int gap;
		private final NodeModel node;
		private final int oldHgap;
		private final int oldParentVgap;
		private final int oldShiftY;
		private final int parentVGap;
		private final int shiftY;

		private ChangeNodePositionActor(final NodeModel node, final int parentVGap, final int gap, final int shiftY) {
			this.node = node;
			this.gap = gap;
			this.shiftY = shiftY;
			this.parentVGap = parentVGap;
			final LocationModel locationModel = LocationModel.getModel(node);
			oldHgap = locationModel.getHGap();
			oldShiftY = locationModel.getShiftY();
			oldParentVgap = !node.isRoot() ? LocationModel.getModel(node.getParentNode()).getVGap() : 0;
		}

		public void act() {
			moveNodePosition(node, parentVGap, gap, shiftY);
		}

		public String getDescription() {
			return "moveNodePosition";
		}

		private void moveNodePosition(final NodeModel node, final int parentVGap, final int hGap, final int shiftY) {
			final LocationModel locationModel = LocationModel.createLocationModel(node);
			locationModel.setHGap(hGap);
			locationModel.setShiftY(shiftY);
			if (!node.isRoot() && this.parentVGap >= 0) {
				LocationModel.createLocationModel(node.getParentNode()).setVGap(parentVGap);
			}
			Controller.getCurrentModeController().getMapController().nodeChanged(node);
		}

		public void undo() {
			moveNodePosition(node, oldParentVgap, oldHgap, oldShiftY);
		}
	}

	public MLocationController() {
		super();
		createActions();
	}

	private void createActions() {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addAction(new ResetNodeLocationAction());
	}

	public void moveNodePosition(final NodeModel node, final int parentVGap, final int hGap, final int shiftY) {
		final IActor actor = new ChangeNodePositionActor(node, parentVGap, hGap, shiftY);
		Controller.getCurrentModeController().execute(actor, node.getMap());
	}
}
