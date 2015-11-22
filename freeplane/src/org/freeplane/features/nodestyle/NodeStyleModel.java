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
package org.freeplane.features.nodestyle;

import java.awt.Color;

import javax.swing.SwingConstants;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev 20.11.2008
 */
public class NodeStyleModel implements IExtension, Cloneable {
	public enum Shape{fork, bubble, small_bubble, oval, circle, wide_hexagon, small_wide_hexagon, narrow_hexagon, as_parent, combined}
	
	public enum TextAlign {
		DEFAULT(SwingConstants.LEFT), 
		LEFT(SwingConstants.LEFT), 
		RIGHT(SwingConstants.RIGHT), 
		CENTER(SwingConstants.CENTER);
		
		final public int swingConstant;

		TextAlign(int swingConstant){
			this.swingConstant = swingConstant;}
	};

	public static NodeStyleModel createNodeStyleModel(final NodeModel node) {
		NodeStyleModel styleModel = node.getExtension(NodeStyleModel.class);
		if (styleModel == null) {
			styleModel = new NodeStyleModel();
			node.addExtension(styleModel);
		}
		return styleModel;
	}

	public static Color getBackgroundColor(final NodeModel node) {
		final NodeStyleModel styleModel = NodeStyleModel.getModel(node);
		return styleModel == null ? null : styleModel.getBackgroundColor();
	}

	public static Color getColor(final NodeModel node) {
		final NodeStyleModel styleModel = node.getExtension(NodeStyleModel.class);
		return styleModel == null ? null : styleModel.getColor();
	}

	public static String getFontFamilyName(final NodeModel node) {
		final NodeStyleModel styleModel = node.getExtension(NodeStyleModel.class);
		return styleModel == null ? null : styleModel.getFontFamilyName();
	}

	public static Integer getFontSize(final NodeModel node) {
		final NodeStyleModel styleModel = node.getExtension(NodeStyleModel.class);
		return styleModel == null ? null : styleModel.getFontSize();
	}

	public static NodeStyleModel getModel(final NodeModel node) {
		final NodeStyleModel styleModel = node.getExtension(NodeStyleModel.class);
		return styleModel;
	}

	public static Boolean getNodeNumbering(NodeModel node) {
		final NodeStyleModel styleModel = node.getExtension(NodeStyleModel.class);
		return styleModel == null ? null : styleModel.getNodeNumbering();
    }

	public static String getNodeFormat(final NodeModel node) {
		final NodeStyleModel styleModel = node.getExtension(NodeStyleModel.class);
		return styleModel == null ? null : styleModel.getNodeFormat();
	}

	public static Shape getShape(final NodeModel node) {
		final NodeStyleModel styleModel = node.getExtension(NodeStyleModel.class);
		return styleModel == null ? null : styleModel.getShape();
	}

	public static Boolean isBold(final NodeModel node) {
		final NodeStyleModel styleModel = node.getExtension(NodeStyleModel.class);
		return styleModel == null ? null : styleModel.isBold();
	}

	public static Boolean isItalic(final NodeModel node) {
		final NodeStyleModel styleModel = node.getExtension(NodeStyleModel.class);
		return styleModel == null ? null : styleModel.isItalic();
	}

	public static TextAlign getTextAlign(final NodeModel node) {
		final NodeStyleModel styleModel = node.getExtension(NodeStyleModel.class);
		return styleModel == null ? null : styleModel.getTextAlign();
	}

	public static void setBackgroundColor(final NodeModel node, final Color color) {
		final NodeStyleModel styleModel = NodeStyleModel.createNodeStyleModel(node);
		styleModel.setBackgroundColor(color);
	}

	public static void setColor(final NodeModel node, final Color color) {
		final NodeStyleModel styleModel = NodeStyleModel.createNodeStyleModel(node);
		styleModel.setColor(color);
	}

	public static void setNodeNumbering(NodeModel node, Boolean enableNodeNumbering) {
		final NodeStyleModel styleModel = NodeStyleModel.createNodeStyleModel(node);
		styleModel.setNodeNumbering(enableNodeNumbering);
    }

	public static void setNodeFormat(final NodeModel node, final String nodeFormat) {
		final NodeStyleModel styleModel = NodeStyleModel.createNodeStyleModel(node);
		styleModel.setNodeFormat(nodeFormat);
	}

	public static void setShape(final NodeModel node, final String shape) {
		final NodeStyleModel styleModel = NodeStyleModel.createNodeStyleModel(node);
		styleModel.setShape(shape);
	}

	public static void setShape(final NodeModel node, final Shape shape) {
		final NodeStyleModel styleModel = NodeStyleModel.createNodeStyleModel(node);
		styleModel.setShape(shape);
	}

	public static void setTextAlign(final NodeModel node, final TextAlign textAlign) {
		final NodeStyleModel styleModel = NodeStyleModel.createNodeStyleModel(node);
		styleModel.setTextAlign(textAlign);
	}

	private Color backgroundColor;
	private Color color;
	private String fontFamilyName = null;
	private Integer fontSize = null;
	private Boolean isBold = null;
	private Boolean isItalic = null;
	private Shape shape;
	private Boolean nodeNumbering = null;
	private String nodeFormat = null;
	private  TextAlign textAlign = null;

	@Override
	protected NodeStyleModel clone() {
		return copyTo(new NodeStyleModel());
	}

	public NodeStyleModel copyTo(final NodeStyleModel nodeStyleModel) {
	    if(color != null)
	        nodeStyleModel.setColor(color);
	    if(backgroundColor != null)
	        nodeStyleModel.setBackgroundColor(backgroundColor);
	    if(isBold != null)
	        nodeStyleModel.setBold(isBold);
	    if(fontFamilyName != null)
	        nodeStyleModel.setFontFamilyName(fontFamilyName);
	    if(fontSize != null)
	        nodeStyleModel.setFontSize(fontSize);
	    if(isItalic != null)
	        nodeStyleModel.setItalic(isItalic);
	    if(shape != null)
	        nodeStyleModel.setShape(shape);
	    if(nodeFormat != null)
	            nodeStyleModel.setNodeFormat(nodeFormat);
	    if(nodeNumbering != null)
	        nodeStyleModel.setNodeNumbering(nodeNumbering);
	    if(textAlign != null)
	    	nodeStyleModel.setTextAlign(textAlign);
		return nodeStyleModel;
    }

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public Color getColor() {
		return color;
	}

	public String getFontFamilyName() {
		return fontFamilyName;
	};

	public Integer getFontSize() {
		return fontSize;
	}
	
	public Boolean getNodeNumbering() {
		return nodeNumbering;
	}

	public String getNodeFormat() {
	    return nodeFormat;
    }

	public Shape getShape() {
		return shape;
	}

	public Boolean isBold() {
		return isBold;
	}

	public Boolean isItalic() {
		return isItalic;
	}

	public TextAlign getTextAlign() {
		return textAlign;
	}

	public void setBackgroundColor(final Color color) {
		backgroundColor = color;
	}

	public void setBold(final Boolean isBold) {
		this.isBold = isBold;
	}

	public void setColor(final Color color) {
		this.color = color;
	}

	public void setFontFamilyName(final String fontFamilyName) {
		this.fontFamilyName = fontFamilyName;
	}

	public void setFontSize(final Integer fontSize) {
		this.fontSize = fontSize;
	}

	public void setItalic(final Boolean isItalic) {
		this.isItalic = isItalic;
	}

	public void setNodeNumbering(Boolean enableNodeNumbering) {
		this.nodeNumbering = enableNodeNumbering;
    }

	public void setNodeFormat(String nodeFormat) {
		this.nodeFormat = nodeFormat;
    }

	public void setShape(final String shape) {
		try {
			if(shape != null)
				this.shape = Shape.valueOf(shape);
			else
				this.shape = null;
		} catch (IllegalArgumentException e) {
			LogUtils.warn("unknown shape " + shape, e);
		}
	}
	
	public void setShape(final Shape shape) {
		this.shape = shape;
	}
	
	public void setTextAlign(final TextAlign textAlign) {
		this.textAlign = textAlign;
	}
}
