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
final class ChangeShiftXActor implements IActor {
	private final Quantity<LengthUnit> shiftX;
	private final NodeModel node;
	private final Quantity<LengthUnit> oldShiftX;

	ChangeShiftXActor(final NodeModel node, final Quantity<LengthUnit> shiftX){
		final LocationModel locationModel = LocationModel.getModel(node);
		oldShiftX = locationModel.getHGap();
		this.node = node;
		this.shiftX = shiftX;
	}

	public void act() {
		setShiftX(node, shiftX);
	}

	public String getDescription() {
		return "moveNodePosition";
	}

	private void setShiftX(final NodeModel node, final Quantity<LengthUnit> hGap) {
		final LocationModel locationModel = LocationModel.createLocationModel(node);
		locationModel.setHGap(hGap);
		Controller.getCurrentModeController().getMapController().nodeChanged(node);
	}

	public void undo() {
		setShiftX(node, oldShiftX);
	}
}