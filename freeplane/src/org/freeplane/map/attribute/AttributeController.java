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
import org.freeplane.map.tree.MapController;
import org.freeplane.modes.ModeController;

/**
 * @author Dimitry Polivaev 22.11.2008
 */
public class AttributeController {
	public AttributeController(final ModeController modeController) {
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final AttributeBuilder attributeBuilder = new AttributeBuilder(
		    mapController);
		attributeBuilder.registerBy(readManager);
	}
}
