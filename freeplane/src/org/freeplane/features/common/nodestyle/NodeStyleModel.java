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
package org.freeplane.features.common.nodestyle;

import java.awt.Color;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.model.NodeModel;

/**
 * @author Dimitry Polivaev 20.11.2008
 */
public class NodeStyleModel implements IExtension, Cloneable {
	public static final String[] NODE_STYLES = new String[] { NodeStyleModel.STYLE_FORK, NodeStyleModel.STYLE_BUBBLE,
	        NodeStyleModel.SHAPE_AS_PARENT, NodeStyleModel.SHAPE_COMBINED };
	public static final String SHAPE_AS_PARENT = "as_parent";
	public static final String SHAPE_COMBINED = "combined";
	public static final String STYLE_BUBBLE = "bubble";
	public static final String STYLE_FORK = "fork";

	public static NodeStyleModel createNodeStyleModel(final NodeModel node) {
		NodeStyleModel styleModel = (NodeStyleModel) node.getExtension(NodeStyleModel.class);
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
		final NodeStyleModel styleModel = (NodeStyleModel) node.getExtension(NodeStyleModel.class);
		return styleModel == null ? null : styleModel.getColor();
	}

	public static String getFontFamilyName(final NodeModel node) {
		final NodeStyleModel styleModel = (NodeStyleModel) node.getExtension(NodeStyleModel.class);
		return styleModel == null ? null : styleModel.getFontFamilyName();
	}

	public static Integer getFontSize(final NodeModel node) {
		final NodeStyleModel styleModel = (NodeStyleModel) node.getExtension(NodeStyleModel.class);
		return styleModel == null ? null : styleModel.getFontSize();
	}

	public static NodeStyleModel getModel(final NodeModel node) {
		final NodeStyleModel styleModel = (NodeStyleModel) node.getExtension(NodeStyleModel.class);
		return styleModel;
	}

	public static String getShape(final NodeModel node) {
		final NodeStyleModel styleModel = (NodeStyleModel) node.getExtension(NodeStyleModel.class);
		return styleModel == null ? null : styleModel.getShape();
	}

	public static Boolean isBold(final NodeModel node) {
		final NodeStyleModel styleModel = (NodeStyleModel) node.getExtension(NodeStyleModel.class);
		return styleModel == null ? null : styleModel.isBold();
	}

	public static Boolean isItalic(final NodeModel node) {
		final NodeStyleModel styleModel = (NodeStyleModel) node.getExtension(NodeStyleModel.class);
		return styleModel == null ? null : styleModel.isItalic();
	}

	public static void setBackgroundColor(final NodeModel node, final Color color) {
		final NodeStyleModel styleModel = NodeStyleModel.createNodeStyleModel(node);
		styleModel.setBackgroundColor(color);
	}

	public static void setColor(final NodeModel node, final Color color) {
		final NodeStyleModel styleModel = NodeStyleModel.createNodeStyleModel(node);
		styleModel.setColor(color);
	}

	public static void setShape(final NodeModel node, final String shape) {
		final NodeStyleModel styleModel = NodeStyleModel.createNodeStyleModel(node);
		styleModel.setShape(shape);
	}

	private Color backgroundColor;
	private Color color;
	private String fontFamilyName = null;
	private Integer fontSize = null;
	private Boolean isBold = null;
	private Boolean isItalic = null;
	private String shape;

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

	public String getShape() {
		return shape;
	}

	public Boolean isBold() {
		return isBold;
	}

	public Boolean isItalic() {
		return isItalic;
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

	public void setShape(final String shape) {
		this.shape = shape;
	}
}
