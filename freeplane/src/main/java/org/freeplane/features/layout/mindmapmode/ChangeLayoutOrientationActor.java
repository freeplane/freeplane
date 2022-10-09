/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2014 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.features.layout.mindmapmode;

import org.freeplane.api.LayoutOrientation;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.layout.LayoutModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 */
class ChangeLayoutOrientationActor implements IActor {
	private final NodeModel node;
	private final LayoutOrientation oldOrientation;
	private final LayoutOrientation newOrientation;

	ChangeLayoutOrientationActor(final NodeModel node, LayoutOrientation newOrientation){
		final LayoutModel layoutModel = LayoutModel.getModel(node);
		oldOrientation = layoutModel.getLayoutOrientation();
		this.node = node;
		this.newOrientation = newOrientation;
	}

	@Override
    public void act() {
		setOrientation(node, oldOrientation, newOrientation);
	}

	@Override
    public String getDescription() {
		return "changeLayoutOrientation";
	}

	private void setOrientation(final NodeModel node, LayoutOrientation oldOrientation, LayoutOrientation newOrientation) {
		if(oldOrientation != newOrientation) {
			LayoutModel.createLayoutModel(node).setLayoutOrientation(newOrientation);
			Controller.getCurrentModeController().getMapController()
			.nodeChanged(node, LayoutOrientation.class, oldOrientation, newOrientation);
		}
	}

	@Override
    public void undo() {
		setOrientation(node, newOrientation, oldOrientation);
	}
}