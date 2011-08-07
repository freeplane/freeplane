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

import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.ConditionalStyleModel;
import org.freeplane.features.styles.MapStyleModel;

/**
 * @author Dimitry Polivaev
 * Jul 21, 2011
 */
public class ManageMapConditionalStylesAction extends AManageConditionalStylesAction{
	
	public static final String NAME = "ManageConditionalStylesAction";
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

	public ManageMapConditionalStylesAction() {
	    super(NAME);
    }

	public ConditionalStyleModel getConditionalStyleModel() {
		final Controller controller = Controller.getCurrentController();
		final MapModel map = controller.getMap();
	    final MapStyleModel styleModel = MapStyleModel.getExtension(map);
		final ConditionalStyleModel conditionalStyleModel = styleModel.getConditionalStyleModel();
	    return conditionalStyleModel;
    }
}
