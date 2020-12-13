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
package org.freeplane.features.nodelocation.mindmapmode;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodelocation.LocationModel;

/**
 * @author Dimitry Polivaev
 * 01.03.2014
 */
final class ChangeShiftYActor implements IActor {
	private final NodeModel node;
	private final Quantity<LengthUnit> oldShiftY;
	private final Quantity<LengthUnit> shiftY;

	ChangeShiftYActor(final NodeModel node, final Quantity<LengthUnit> shiftY){
		final LocationModel locationModel = LocationModel.getModel(node);
		oldShiftY = locationModel.getShiftY();
		this.node = node;
		this.shiftY = shiftY;
	}

	public void act() {
		setShiftY(node, shiftY);
	}

	public String getDescription() {
		return "moveNodePosition";
	}

	private void setShiftY(final NodeModel node, final Quantity<LengthUnit> shiftY) {
		final LocationModel locationModel = LocationModel.createLocationModel(node);
		locationModel.setShiftY(shiftY);
		Controller.getCurrentModeController().getMapController().nodeChanged(node);
	}

	public void undo() {
		setShiftY(node, oldShiftY);
	}
}