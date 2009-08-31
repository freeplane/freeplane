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
package org.freeplane.features.common.edge;

import java.awt.Color;
import java.io.IOException;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.IExtensionElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.n3.nanoxml.XMLElement;

class EdgeBuilder implements IElementDOMHandler, IExtensionElementWriter {
	public EdgeBuilder() {
	}

	protected EdgeModel createEdge(final NodeModel node) {
		return EdgeModel.createEdgeModel(node);
	}

	public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
		if (tag.equals("edge")) {
			return createEdge((NodeModel) parent);
		}
		return null;
	}

	public void endElement(final Object parent, final String tag, final Object userObject, final XMLElement dom) {
		/* attributes */
		if (parent instanceof NodeModel) {
			final NodeModel node = (NodeModel) parent;
			if (userObject instanceof EdgeModel) {
				final EdgeModel edge = (EdgeModel) userObject;
				EdgeModel.setModel(node, edge);
			}
			return;
		}
	}

	private void registerAttributeHandlers(final ReadManager reader) {
		reader.addAttributeHandler("edge", "STYLE", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final EdgeModel edge = (EdgeModel) userObject;
				edge.setStyle(EdgeStyle.getStyle(value));
			}
		});
		reader.addAttributeHandler("edge", "HIDE", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final EdgeModel edge = (EdgeModel) userObject;
				edge.setStyle(EdgeStyle.EDGESTYLE_HIDDEN);
			}
		});
		reader.addAttributeHandler("edge", "COLOR", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final EdgeModel edge = (EdgeModel) userObject;
				edge.setColor(ColorUtils.stringToColor(value.toString()));
			}
		});
		reader.addAttributeHandler("edge", "WIDTH", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final EdgeModel edge = (EdgeModel) userObject;
				if (value.equals(EdgeModel.EDGEWIDTH_THIN)) {
					edge.setWidth(EdgeModel.WIDTH_THIN);
				}
				else {
					edge.setWidth(Integer.parseInt(value.toString()));
				}
			}
		});
	}

	/**
	 */
	public void registerBy(final ReadManager reader, final WriteManager writer) {
		reader.addElementHandler("edge", this);
		registerAttributeHandlers(reader);
		writer.addExtensionElementWriter(EdgeModel.class, this);
	}

	public void setAttributes(final String tag, final Object node, final XMLElement attributes) {
	}

	public void writeContent(final ITreeWriter writer, final Object node, final IExtension extension)
	        throws IOException {
		final EdgeModel model = (EdgeModel) extension;
		final String style = EdgeStyle.toString(model.getStyle());
		final Color color = model.getColor();
		final int width = model.getWidth();
		if (style != null || color != null || width != EdgeModel.DEFAULT_WIDTH) {
			final XMLElement edge = new XMLElement();
			edge.setName("edge");
			boolean relevant = false;
			if (style != null) {
				if (style.equals(EdgeStyle.EDGESTYLE_HIDDEN)) {
					edge.setAttribute("HIDE", "true");
					relevant = true;
				}
				edge.setAttribute("STYLE", style);
				relevant = true;
			}
			if (color != null) {
				edge.setAttribute("COLOR", ColorUtils.colorToString(color));
				relevant = true;
			}
			if (width != EdgeModel.WIDTH_PARENT) {
				if (width == EdgeModel.WIDTH_THIN) {
					edge.setAttribute("WIDTH", EdgeModel.EDGEWIDTH_THIN);
				}
				else {
					edge.setAttribute("WIDTH", Integer.toString(width));
				}
				relevant = true;
			}
			if (relevant) {
				writer.addElement(model, edge);
			}
		}
	}
}
