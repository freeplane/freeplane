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
package org.freeplane.features.nodestyle;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 * Nov 13, 2011
 */
public class NodeSizeModel implements IExtension {
	public static final int NOT_SET = -1;

	public static NodeSizeModel getModel(final NodeModel node) {
		return node.getExtension(NodeSizeModel.class);
	}
	
	public static NodeSizeModel createNodeSizeModel(final NodeModel node) {
		NodeSizeModel styleModel = node.getExtension(NodeSizeModel.class);
		if (styleModel == null) {
			styleModel = new NodeSizeModel();
			node.addExtension(styleModel);
		}
		return styleModel;
	}

	private int minNodeWidth = NOT_SET;
	private int maxTextWidth = NOT_SET;
	
	public int getMaxNodeWidth() {
    	return maxTextWidth;
    }
	public void setMaxNodeWidth(int maxTextWidth) {
    	this.maxTextWidth = maxTextWidth;
    }
	public int getMinNodeWidth() {
    	return minNodeWidth;
    }
	public void setMinNodeWidth(int minNodeWidth) {
    	this.minNodeWidth = minNodeWidth;
    }
	
	public static void setNodeMaxNodeWidth(NodeModel node, int maxTextWidth) {
		createNodeSizeModel(node).setMaxNodeWidth(maxTextWidth);
    }
	public static void setNodeMinWidth(NodeModel node, int minNodeWidth) {
		createNodeSizeModel(node).setMinNodeWidth(minNodeWidth);
    }
	
	public static int getNodeMaxNodeWidth(NodeModel node) {
		final NodeSizeModel extension = node.getExtension(NodeSizeModel.class);
		return extension != null ? extension.getMaxNodeWidth() : NOT_SET;
    }
	public static int getMinNodeWidth(NodeModel node) {
		final NodeSizeModel extension = node.getExtension(NodeSizeModel.class);
		return extension != null ? extension.getMinNodeWidth() : NOT_SET;
    }

	public void copyTo(NodeSizeModel to) {
	    if(maxTextWidth != NOT_SET)
	    	to.setMaxNodeWidth(maxTextWidth);
	    if(minNodeWidth != NOT_SET)
	    	to.setMinNodeWidth(minNodeWidth);
    }
}
