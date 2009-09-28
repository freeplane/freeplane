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
package org.freeplane.features.common.addins.mapstyle;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.model.NodeModel;

/**
 * @author Dimitry Polivaev
 * 28.09.2009
 */
public class NodeStyleModel implements IExtension {
	private String style;

	public String getStyle() {
    	return style;
    }

	public void setStyle(String style) {
    	this.style = style;
    }
	
	static public NodeStyleModel getExtension(NodeModel node){
		return (NodeStyleModel) node.getExtension(NodeStyleModel.class);
	}
	
	static public NodeStyleModel createExtension(NodeModel node){
		NodeStyleModel extension = (NodeStyleModel) node.getExtension(NodeStyleModel.class);
		if(extension == null){
			extension = new NodeStyleModel();
			node.addExtension(extension);
		}
		return extension;
	}
}
