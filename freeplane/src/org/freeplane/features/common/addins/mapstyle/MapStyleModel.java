/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry Polivaev
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
package org.freeplane.features.common.addins.mapstyle;

import java.awt.Color;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.model.NodeModel;

/**
 * @author Dimitry Polivaev
 * Mar 12, 2009
 */
public class MapStyleModel implements IExtension{
	private Color backgroundColor;

	public MapStyleModel() {
    }

	protected void setBackgroundColor(Color backgroundColor) {
    	this.backgroundColor = backgroundColor;
    }

	protected Color getBackgroundColor() {
    	return backgroundColor;
    }

	public static MapStyleModel createExtension(NodeModel node) {
        MapStyleModel extension = (MapStyleModel)node.getExtension(MapStyleModel.class);
        if(extension == null){
        	extension = new MapStyleModel();
        	node.addExtension(extension);
        }
		return extension;
    }
	public static MapStyleModel getExtension(NodeModel node) {
        return (MapStyleModel)node.getExtension(MapStyleModel.class);
    }
}