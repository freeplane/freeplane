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

import java.awt.Color;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.extension.IExtension;
import org.freeplane.features.DashVariant;
import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 * Nov 13, 2011
 */
public class NodeBorderModel implements IExtension {
	
	public static NodeBorderModel getModel(final NodeModel node) {
		return node.getExtension(NodeBorderModel.class);
	}
	
	public static NodeBorderModel createNodeBorderModel(final NodeModel node) {
		NodeBorderModel styleModel = node.getExtension(NodeBorderModel.class);
		if (styleModel == null) {
			styleModel = new NodeBorderModel();
			node.addExtension(styleModel);
		}
		return styleModel;
	}

	private Boolean borderWidthMatchesEdgeWidth = null;
	private Quantity<LengthUnit> borderWidth = null;

	private Boolean borderDashMatchesEdgeDash = null;
	private DashVariant borderDash = null;

	private Boolean borderColorMatchesEdgeColor = null;
	private Color borderColor = null;

	public Boolean getBorderWidthMatchesEdgeWidth() {
		return borderWidthMatchesEdgeWidth;
	}
	
	public static Boolean getBorderWidthMatchesEdgeWidth(final NodeModel node) {
		final NodeBorderModel extension = node.getExtension(NodeBorderModel.class);
		return extension == null ? null : extension.getBorderWidthMatchesEdgeWidth();
	}

	public static Quantity<LengthUnit> getBorderWidth(final NodeModel node) {
		final NodeBorderModel extension = node.getExtension(NodeBorderModel.class);
		return extension == null ? null : extension.getBorderWidth();
	}

	public Quantity<LengthUnit> getBorderWidth() {
		return borderWidth;
	}

	public static void setBorderWidthMatchesEdgeWidth(final NodeModel node, final Boolean borderWidthMatchesEdgeWidth) {
		createNodeBorderModel(node).setBorderWidthMatchesEdgeWidth(borderWidthMatchesEdgeWidth);
	}

	public static void setBorderWidth(final NodeModel node, final Quantity<LengthUnit> borderWidth) {
		createNodeBorderModel(node).setBorderWidth(borderWidth);
	}

	public void setBorderWidth(Quantity<LengthUnit> borderWidth) {
		this.borderWidth = borderWidth;
		
	}

	public void setBorderWidthMatchesEdgeWidth(Boolean borderWidthMatchesEdgeWidth) {
		this.borderWidthMatchesEdgeWidth = borderWidthMatchesEdgeWidth;
	}

	public Boolean getBorderDashMatchesEdgeDash() {
		return borderDashMatchesEdgeDash;
	}
	
	public static Boolean getBorderDashMatchesEdgeDash(final NodeModel node) {
		final NodeBorderModel extension = node.getExtension(NodeBorderModel.class);
		return extension == null ? null : extension.getBorderDashMatchesEdgeDash();
	}

	public static DashVariant getBorderDash(final NodeModel node) {
		final NodeBorderModel extension = node.getExtension(NodeBorderModel.class);
		return extension == null ? null : extension.getBorderDash();
	}

	public DashVariant getBorderDash() {
		return borderDash;
	}

	public static void setBorderDashMatchesEdgeDash(final NodeModel node, final Boolean borderDashMatchesEdgeDash) {
		createNodeBorderModel(node).setBorderDashMatchesEdgeDash(borderDashMatchesEdgeDash);
	}

	public static void setBorderDash(final NodeModel node, final DashVariant borderDash) {
		createNodeBorderModel(node).setBorderDash(borderDash);
	}

	public void setBorderDash(DashVariant borderDash) {
		this.borderDash = borderDash;
		
	}

	public void setBorderDashMatchesEdgeDash(Boolean borderDashMatchesEdgeDash) {
		this.borderDashMatchesEdgeDash = borderDashMatchesEdgeDash;
	}

	public Boolean getBorderColorMatchesEdgeColor() {
		return borderColorMatchesEdgeColor;
	}
	
	public static Boolean getBorderColorMatchesEdgeColor(final NodeModel node) {
		final NodeBorderModel extension = node.getExtension(NodeBorderModel.class);
		return extension == null ? null : extension.getBorderColorMatchesEdgeColor();
	}

	public static Color getBorderColor(final NodeModel node) {
		final NodeBorderModel extension = node.getExtension(NodeBorderModel.class);
		return extension == null ? null : extension.getBorderColor();
	}


	public Color getBorderColor() {
		return borderColor;
	}

	public static void setBorderColorMatchesEdgeColor(final NodeModel node, final Boolean borderColorMatchesEdgeColor) {
		createNodeBorderModel(node).setBorderColorMatchesEdgeColor(borderColorMatchesEdgeColor);
	}

	public static void setBorderColor(final NodeModel node, final Color borderColor) {
		createNodeBorderModel(node).setBorderColor(borderColor);
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
		
	}

	public void setBorderColorMatchesEdgeColor(Boolean borderColorMatchesEdgeColor) {
		this.borderColorMatchesEdgeColor = borderColorMatchesEdgeColor;
	}

	public void copyTo(NodeBorderModel to) {
	    if(borderWidthMatchesEdgeWidth != null)
	    	to.setBorderWidthMatchesEdgeWidth(borderWidthMatchesEdgeWidth);
	    if(borderDashMatchesEdgeDash != null)
	    	to.setBorderDashMatchesEdgeDash(borderDashMatchesEdgeDash);
	    if(borderWidth != null)
	    	to.setBorderWidth(borderWidth);
	    if(borderColorMatchesEdgeColor != null)
	    	to.setBorderColorMatchesEdgeColor(borderColorMatchesEdgeColor);
	    if(borderColor != null)
	    	to.setBorderColor(borderColor);
    }
}
