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

import java.util.ArrayList;

import org.freeplane.core.undo.IActor;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodelocation.LocationController;

/**
 * @author Dimitry Polivaev
 */
public class MLocationController extends LocationController {
	public MLocationController() {
		super();
		createActions();
	}

	private void createActions() {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addAction(new ResetNodeLocationAction());
	}

	public void moveNodePosition(final NodeModel node, final int parentVGap, final int hGap, final int shiftY) {
		ArrayList<IActor> actors = new ArrayList<IActor>(3);
		actors.add(new ChangeShiftXActor(node, hGap));
		actors.add(new ChangeShiftYActor(node, shiftY));
		final NodeModel parentNode = node.getParentNode();
		if(parentNode != null)
			actors.add(new ChangeVGapActor(parentNode, parentVGap));
		for (final IActor actor : actors)
		Controller.getCurrentModeController().execute(actor, node.getMap());
	}


	public void setHorizontalShift(NodeModel node, final int horizontalShift){
		final IActor actor = new ChangeShiftXActor(node, horizontalShift);
		Controller.getCurrentModeController().execute(actor, node.getMap());
	}

	public void setVerticalShift(NodeModel node, final int verticalShift){
		final IActor actor = new ChangeShiftYActor(node, verticalShift);
		Controller.getCurrentModeController().execute(actor, node.getMap());
	}

	public void setMinimalDistanceBetweenChildren(NodeModel node, final int minimalDistanceBetweenChildren){
		final IActor actor = new ChangeVGapActor(node, minimalDistanceBetweenChildren);
		Controller.getCurrentModeController().execute(actor, node.getMap());
	}
}
