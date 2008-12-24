/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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
package org.freeplane.map.nodestyle;

import java.awt.Color;
import org.freeplane.extension.IExtension;

/**
 * @author Dimitry Polivaev 20.11.2008
 */
public class NodeStyleModel implements IExtension, Cloneable {
	@Override
    protected Object clone() throws CloneNotSupportedException {
		final NodeStyleModel nodeStyleModel = new NodeStyleModel();
		nodeStyleModel.setColor(color);
		nodeStyleModel.setBackgroundColor(backgroundColor);
		nodeStyleModel.setBold(isBold);
		nodeStyleModel.setFontFamilyName(fontFamilyName);
		nodeStyleModel.setFontSize(fontSize);
		nodeStyleModel.setItalic(isItalic);
		nodeStyleModel.setShape(shape);
		return nodeStyleModel;
    }

	public Boolean isItalic() {
    	return isItalic;
    }

	public void setItalic(Boolean isItalic) {
    	this.isItalic = isItalic;
    }

	public Boolean isBold() {
    	return isBold;
    }

	public void setBold(Boolean isBold) {
    	this.isBold = isBold;
    }

	public void setFontSize(Integer fontSize) {
    	this.fontSize = fontSize;
    }

	public Integer getFontSize() {
    	return fontSize;
    }

	public String getFontFamilyName() {
    	return fontFamilyName;
    }

	public void setFontFamilyName(String fontFamilyName) {
    	this.fontFamilyName = fontFamilyName;
    }

	public static final String[] NODE_STYLES = new String[] { NodeStyleModel.STYLE_FORK,
	        NodeStyleModel.STYLE_BUBBLE, NodeStyleModel.SHAPE_AS_PARENT,
	        NodeStyleModel.SHAPE_COMBINED };
	public static final String SHAPE_AS_PARENT = "as_parent";
	public static final String SHAPE_COMBINED = "combined";
	public static final String STYLE_BUBBLE = "bubble";
	public static final String STYLE_FORK = "fork";
	private Color backgroundColor;
	private Color color;
	private String shape;
	private Boolean isItalic = null;
	private Boolean isBold = null;
	private Integer fontSize = null;
	private String fontFamilyName = null;

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public Color getColor() {
		return color;
	}

	public String getShape() {
		return shape;
	}

	public void setBackgroundColor(final Color color) {
		backgroundColor = color;
	}

	public void setColor(final Color color) {
		this.color = color;
	};

	public void setShape(final String shape) {
		this.shape = shape;
	}
	
	
}
