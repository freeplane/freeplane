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
package org.freeplane.features.nodelocation;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

/**
 * @author Dimitry Polivaev
 */
public class LocationController implements IExtension {
	public static LocationController getController() {
		final ModeController modeController = Controller.getCurrentModeController();
		return getController(modeController);
	}

	public static LocationController getController(ModeController modeController) {
		return modeController.getExtension(LocationController.class);
	}

	public static void install( final LocationController locationController) {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addExtension(LocationController.class, locationController);
	}

// 	final private ModeController modeController;

	public LocationController() {
		super();
//		this.modeController = modeController;
		final ModeController modeController = Controller.getCurrentModeController();
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		final LocationBuilder locationBuilder = new LocationBuilder();
		locationBuilder.registerBy(readManager, writeManager);
	}

	public int getHorizontalShift(NodeModel node){
		return LocationModel.getModel(node).getHGap();
	}

	public int getVerticalShift(NodeModel node){
		return LocationModel.getModel(node).getShiftY();
	}

	public int getMinimalDistanceBetweenChildren(NodeModel node){
		return LocationModel.getModel(node).getVGap();
	}
}
