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

import org.freeplane.controller.Freeplane;
import org.freeplane.map.nodelocation.LocationController;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.mindmapmode.MModeController;

/**
 * @author Dimitry Polivaev
 */
public class MLocationController extends LocationController {
	static private boolean actionsCreated = false;
	static private MoveNodeAction moveNodeAction;

	public MLocationController(final MModeController modeController) {
		super(modeController);
		createActions(modeController);
		modeController.setNodeMotionListener(new MNodeMotionListener(
		    modeController));
	}

	/**
	 * @param modeController
	 */
	private void createActions(final MModeController modeController) {
		if (!actionsCreated) {
			actionsCreated = true;
			moveNodeAction = new MoveNodeAction(modeController);
			Freeplane.getController().addAction("moveNodeAction",
			    moveNodeAction);
		}
	}

	public MModeController getMModeController() {
		return (MModeController) getModeController();
	}

	public void moveNodePosition(final NodeModel node, final int parentVGap,
	                             final int hGap, final int shiftY) {
		moveNodeAction.moveNodeTo(node, parentVGap, hGap, shiftY);
	}
}
