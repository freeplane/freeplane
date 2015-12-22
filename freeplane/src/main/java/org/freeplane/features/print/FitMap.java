/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
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
package org.freeplane.features.print;

import org.freeplane.core.resources.ResourceController;

/**
 * @author Dimitry Polivaev
 * 30.08.2009
 */
public enum FitMap {
	USER_DEFINED, WIDTH, HEIGHT, PAGE, BACKGROUND;
	public static FitMap valueOf() {
		final String fitMapStr = ResourceController.getResourceController().getProperty("fit_map");
		return FitMap.valueOf(fitMapStr);
	}
}
