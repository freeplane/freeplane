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
package org.freeplane.features.filemode;

import java.io.File;

import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;

/**
 * @author Dimitry Polivaev
 */
public class FMapController extends MapController {
	public FMapController(final FModeController modeController) {
		super(modeController);
	}

	public FModeController getFModeController() {
		return (FModeController) getModeController();
	}

	public MapModel newMap(final File file) {
		final FMapModel fileMapModel = new FMapModel(file, getModeController());
		fireMapCreated(fileMapModel);
		newMapView(fileMapModel);
		return fileMapModel;
	}

	@Override
	public NodeModel newNode(final Object userObject, final MapModel map) {
		return new FNodeModel((File) userObject, map);
	}

	public void toggleFolded(final NodeModel node) {
		if (hasChildren(node) && !node.isRoot()) {
			setFolded(node, !isFolded(node));
		}
	}
}
