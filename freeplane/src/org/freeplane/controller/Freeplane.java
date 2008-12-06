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
package org.freeplane.controller;

import org.freeplane.controller.resources.ResourceController;

/**
 * @author Dimitry Polivaev
 */
public class Freeplane {
	private static Controller controller;
	private static ResourceController resourceController;

	public static Controller getController() {
		return controller;
	}

	public static String getText(final String string) {
		return resourceController.getText(string);
	}

	static void setController(final Controller controller) {
		if (Freeplane.controller != null) {
			throw new RuntimeException("Controller already set");
		}
		Freeplane.controller = controller;
		Freeplane.resourceController = controller.getResourceController();
	}
}
