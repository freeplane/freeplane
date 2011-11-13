/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.features.styles.mindmapmode;

import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.MapStyle;

/**
 * @author Dimitry Polivaev
 * Nov 13, 2011
 */
public class MMapStyle extends MapStyle{

	private MMapStyle(boolean persistent) {
	    super(persistent);
    }
	
	public static void install(boolean persistent){
		MapStyle.install(persistent);
		ModeController modeController = Controller.getCurrentModeController();
		modeController.addAction(new MapNodeWidthAction());
	}
	
}
