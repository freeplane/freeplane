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
package org.freeplane.map.tree.browsemode;

import org.freeplane.map.tree.MapController;
import org.freeplane.map.tree.MapModel;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.browsemode.BModeController;
import org.freeplane.modes.mindmapmode.MModeController;

/**
 * @author Dimitry Polivaev
 */
public class BMapController extends MapController {
	public BMapController(final BModeController modeController) {
		super(modeController);
	}

	public MModeController getMModeController() {
		return (MModeController) getModeController();
	}

	@Override
	protected void newMapView(final MapModel mapModel) {
		((BModeController) getModeController()).setNoteIcon(mapModel
		    .getRootNode());
		super.newMapView(mapModel);
	}

	@Override
	public MapModel newModel(final NodeModel root) {
		return new MapModel(getModeController(), root);
	}
}
