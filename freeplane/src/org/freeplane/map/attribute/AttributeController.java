/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.map.attribute;

import org.freeplane.io.ReadManager;
import org.freeplane.io.WriteManager;
import org.freeplane.map.tree.MapController;
import org.freeplane.map.tree.MapReader;
import org.freeplane.modes.ModeController;

/**
 * @author Dimitry Polivaev 22.11.2008
 */
public class AttributeController {
	final private ModeController modeController;

	public AttributeController(final ModeController modeController) {
		this.modeController = modeController;
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		final MapReader mapReader = mapController.getMapReader();
		final AttributeBuilder attributeBuilder = new AttributeBuilder(mapReader);
		attributeBuilder.registerBy(readManager, writeManager);
	}

	protected ModeController getModeController() {
		return modeController;
	}
}
