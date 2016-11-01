/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.features.edge;

import java.awt.Color;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.DashVariant;
import org.freeplane.features.map.NodeModel;

public class EdgeModel implements IExtension {
	public static final int WIDTH_PARENT = -1;
	public static final int DEFAULT_WIDTH = -1;
	static public final String EDGEWIDTH_THIN = "thin";
	public static final int WIDTH_THIN = 0;

	public static EdgeModel createEdgeModel(final NodeModel node) {
		EdgeModel edge = (EdgeModel) node.getExtension(EdgeModel.class);
		if (edge == null) {
			edge = new EdgeModel();
			node.addExtension(edge);
		}
		return edge;
	}

	public static EdgeModel getModel(final NodeModel node) {
		return (EdgeModel) node.getExtension(EdgeModel.class);
	}

	public static void setModel(final NodeModel node, final EdgeModel edge) {
		node.addExtension(edge);
	}

	private Color color;
	private EdgeStyle style;
	private int width = EdgeModel.DEFAULT_WIDTH;
	private DashVariant dash;

	public DashVariant getDash() {
		return dash;
	}

	public void setDash(DashVariant dash) {
		this.dash = dash;
	}

	public EdgeModel() {
	}

	public Color getColor() {
		return color;
	}

	public EdgeStyle getStyle() {
		return style;
	}

	public int getWidth() {
		return width;
	}

	public void setColor(final Color color) {
		this.color = color;
	}

	public void setStyle(final EdgeStyle style) {
		this.style = style;
	}

	public void setWidth(final int width) {
		this.width = width;
	}
}
