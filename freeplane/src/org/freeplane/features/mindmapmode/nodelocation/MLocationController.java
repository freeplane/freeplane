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
package org.freeplane.features.mindmapmode.nodelocation;

import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.common.nodelocation.LocationController;
import org.freeplane.features.common.nodelocation.LocationModel;
import org.freeplane.features.mindmapmode.MModeController;

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

		private ChangeNodePositionActor(final NodeModel node, final int gap, final int shiftY, final int parentVGap) {
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
			if (!node.isRoot()) {
				LocationModel.createLocationModel(node.getParentNode()).setVGap(parentVGap);
			}
			getModeController().getMapController().nodeChanged(node);
		}

		public void undo() {
			moveNodePosition(node, oldParentVgap, oldHgap, oldShiftY);
		}
	}

	public MLocationController(final MModeController modeController) {
		super(modeController);
		createActions(modeController);
	}

	private void createActions(final ModeController modeController) {
		modeController.addAction(new ResetNodeLocationAction(modeController.getController()));
	}

	public void moveNodePosition(final NodeModel node, final int parentVGap, final int hGap, final int shiftY) {
		final IActor actor = new ChangeNodePositionActor(node, hGap, shiftY, parentVGap);
		getModeController().execute(actor, node.getMap());
	}
}
