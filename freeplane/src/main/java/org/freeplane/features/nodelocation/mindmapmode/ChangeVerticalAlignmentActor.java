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

import org.freeplane.api.VerticalNodeAlignment;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodelocation.LocationModel;

/**
 * @author Dimitry Polivaev
 */
class ChangeVerticalAlignmentActor implements IActor {
	private final NodeModel node;
	private final VerticalNodeAlignment oldAlignment;
	private final VerticalNodeAlignment newAlignment;

	ChangeVerticalAlignmentActor(final NodeModel node, VerticalNodeAlignment newAlignment){
		final LocationModel locationModel = LocationModel.getModel(node);
		oldAlignment = locationModel.getVerticalAlignment();
		this.node = node;
		this.newAlignment = newAlignment;
	}

	public void act() {
		setAlignment(node, oldAlignment, newAlignment);
	}

	public String getDescription() {
		return "changeVerticalAlignment";
	}

	private void setAlignment(final NodeModel node, VerticalNodeAlignment oldAlignment, VerticalNodeAlignment newAlignment) {
		if(oldAlignment != newAlignment) {
			LocationModel.createLocationModel(node).setVerticalAlignment(newAlignment);
			Controller.getCurrentModeController().getMapController()
			.nodeChanged(node, VerticalNodeAlignment.class, oldAlignment, newAlignment);
		}
	}

	public void undo() {
		setAlignment(node, newAlignment, oldAlignment);
	}
}