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
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Convertible;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 * Nov 13, 2011
 */
public class NodeSizeModel implements IExtension {
	
	public enum LengthUnits implements Convertible{
/*
+---------+-------------+---------------+
| px      | Pixels      | Varies        | 
+---------+-------------+---------------+
| in      | Inches      | 1             | 
+---------+-------------+---------------+
| mm      | Millimeters | 25.4          | 
+---------+-------------+---------------+
| cm      | Centimeters | 2.54          | 
+---------+-------------+---------------+
| pt      | Points      | 72            | 
+---------+-------------+---------------+
		
 */
		px(1d), 
		in(UITools.getScreenResolution()), 
		mm(UITools.getScreenResolution() / 25.4), 
		cm(UITools.getScreenResolution() / 2.54);
		
		LengthUnits(double factor){
			this.factor = factor;
			
		}
		final private double factor;
		
		public double factor() {
			return factor;
		}
		
	}
	
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

	private Quantity<LengthUnits> minNodeWidth = null;
	private Quantity<LengthUnits> maxTextWidth = null;
	
	public Quantity<LengthUnits> getMaxNodeWidth() {
    	return maxTextWidth;
    }
	public void setMaxNodeWidth(Quantity<LengthUnits> maxTextWidth) {
    	this.maxTextWidth = maxTextWidth;
    }

	public Quantity<LengthUnits> getMinNodeWidth() {
    	return minNodeWidth;
    }
	
	public void setMinNodeWidth(Quantity<LengthUnits> quantity) {
		this.minNodeWidth = quantity;
	}
	
	public static void setMaxNodeWidth(NodeModel node, Quantity<LengthUnits> maxTextWidth) {
		createNodeSizeModel(node).setMaxNodeWidth(maxTextWidth);
    }
	public static void setNodeMinWidth(NodeModel node, Quantity<LengthUnits> minNodeWidth) {
		createNodeSizeModel(node).setMinNodeWidth(minNodeWidth);
    }
	
	public static Quantity<LengthUnits> getMaxNodeWidth(NodeModel node) {
		final NodeSizeModel extension = node.getExtension(NodeSizeModel.class);
		return extension != null ? extension.getMaxNodeWidth() : null;
    }
	public static Quantity<LengthUnits> getMinNodeWidth(NodeModel node) {
		final NodeSizeModel extension = node.getExtension(NodeSizeModel.class);
		return extension != null ? extension.getMinNodeWidth() : null;
    }

	public void copyTo(NodeSizeModel to) {
	    if(maxTextWidth != null)
	    	to.setMaxNodeWidth(maxTextWidth);
	    if(minNodeWidth != null)
	    	to.setMinNodeWidth(minNodeWidth);
    }
}
