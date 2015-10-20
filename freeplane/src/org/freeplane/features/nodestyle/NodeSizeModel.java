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
	
	enum LengthUnits implements Convertible{
/*
 * px      | Pixels      | Varies        | No          | No                 | 
+---------+-------------+---------------+-------------+--------------------+
| in      | Inches      | 1             | Yes         | Yes                | 
+---------+-------------+---------------+-------------+--------------------+
| mm      | Millimeters | 25.4          | Yes         | Yes                | 
+---------+-------------+---------------+-------------+--------------------+
| pt      | Points      | 72      
		
 */
		px(1d / UITools.getScreenResolution()), in(1), mm(1d / 25.4), cm(1d / 2.54);
		
		LengthUnits(double factor){
			this.factor = factor * UITools.getScreenResolution();
			
		}
		final private double factor;
		@Override
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
	
	public int getMaxNodeWidth() {
    	return maxTextWidth != null ?  maxTextWidth.inBaseUnitsRounded() : NOT_SET;
    }
	public void setMaxNodeWidth(int maxTextWidth) {
		setMaxNodeWidth(maxTextWidth != NOT_SET ? new Quantity<NodeSizeModel.LengthUnits>(maxTextWidth, LengthUnits.px) : null);
    }
	
	public void setMaxNodeWidth(Quantity<LengthUnits> maxTextWidth) {
    	this.maxTextWidth = maxTextWidth;
    }
	public int getMinNodeWidth() {
    	return minNodeWidth != null ?  minNodeWidth.inBaseUnitsRounded() : NOT_SET;
    }
	
	public void setMinNodeWidth(int minNodeWidth) {
    	setMinNodeWidth(minNodeWidth != NOT_SET ? new Quantity<NodeSizeModel.LengthUnits>(minNodeWidth, LengthUnits.px) : null);
    }

	public void setMinNodeWidth(Quantity<LengthUnits> quantity) {
		this.minNodeWidth = quantity;
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
	    if(maxTextWidth != null)
	    	to.setMaxNodeWidth(maxTextWidth);
	    if(minNodeWidth != null)
	    	to.setMinNodeWidth(minNodeWidth);
    }
}
