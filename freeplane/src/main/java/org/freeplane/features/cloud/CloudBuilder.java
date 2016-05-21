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
package org.freeplane.features.cloud;

import java.awt.Color;
import java.io.IOException;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.IElementWriter;
import org.freeplane.core.io.IExtensionElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapWriter;
import org.freeplane.features.map.NodeBuilder;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

class CloudBuilder implements IElementDOMHandler, IExtensionElementWriter, IElementWriter {
//	private final MapController mapController;
	private final CloudController cc;

	public CloudBuilder(final MapController mapController, final CloudController cc) {
//		this.mapController = mapController;
		this.cc = cc;
	}

	public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
		if (tag.equals("cloud")) {
			final CloudModel oldCloud = CloudModel.getModel((NodeModel) parent);
			return oldCloud != null ? oldCloud : new CloudModel();
		}
		return null;
	}

	public void endElement(final Object parent, final String tag, final Object userObject, final XMLElement dom) {
		if (parent instanceof NodeModel) {
			final NodeModel node = (NodeModel) parent;
			if (userObject instanceof CloudModel) {
				final CloudModel cloud = (CloudModel) userObject;
				CloudModel.setModel(node, cloud);
			}
		}
	}

	private void registerAttributeHandlers(final ReadManager reader) {
		reader.addAttributeHandler("cloud", "STYLE", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				//				final CloudModel cloud = (CloudModel) userObject;
				//				cloud.setStyle(value.toString());
			}
		});
		reader.addAttributeHandler("cloud", "COLOR", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final CloudModel cloud = (CloudModel) userObject;
				cloud.setColor(ColorUtils.stringToColor(value, cloud.getColor()));
			}
		});
		reader.addAttributeHandler("cloud", "ALPHA", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final CloudModel cloud = (CloudModel) userObject;
				cloud.setColor(ColorUtils.alphaToColor(value, cloud.getColor()));
			}
		});
		reader.addAttributeHandler("cloud", "SHAPE", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final CloudModel cloud = (CloudModel) userObject;
				cloud.setShape(CloudModel.Shape.valueOf(value));
			}
		});
		reader.addAttributeHandler("cloud", "WIDTH", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				//				final CloudModel cloud = (CloudModel) userObject;
				//				cloud.setWidth(Integer.parseInt(value.toString()));
			}
		});
	}

	/**
	 */
	public void registerBy(final ReadManager reader, final WriteManager writer) {
		reader.addElementHandler("cloud", this);
		registerAttributeHandlers(reader);
		writer.addExtensionElementWriter(CloudModel.class, this);
		writer.addElementWriter(NodeBuilder.XML_NODE, this);
		writer.addElementWriter(NodeBuilder.XML_STYLENODE, this);
	}

	public void setAttributes(final String tag, final Object node, final XMLElement attributes) {
	}

	public void writeContent(final ITreeWriter writer, final Object userObject, final String tag) throws IOException {
		final boolean forceFormatting = Boolean.TRUE.equals(writer.getHint(MapWriter.WriterHint.FORCE_FORMATTING));
		if (!forceFormatting) {
			return;
		}
		writeContentImpl(writer, (NodeModel) userObject, null);
	}

	public void writeContent(final ITreeWriter writer, final Object userObject, final IExtension extension)
	        throws IOException {
		final boolean forceFormatting = Boolean.TRUE.equals(writer.getHint(MapWriter.WriterHint.FORCE_FORMATTING));
		if (forceFormatting) {
			return;
		}
		writeContentImpl(writer, null, extension);
	}

	private void writeContentImpl(final ITreeWriter writer, final NodeModel node, final IExtension extension)
	        throws IOException {
		final CloudModel model = extension != null ? (CloudModel) extension : cc.getCloud(node);
		if (model == null) {
			return;
		}
		final XMLElement cloud = new XMLElement();
		cloud.setName("cloud");
		//		final String style = model.getStyle();
		//		if (style != null) {
		//			cloud.setAttribute("STYLE", style);
		//		}
		final Color color = model.getColor();
		if (color != null) {
			ColorUtils.setColorAttributes(cloud, "COLOR", "ALPHA", color);
		}
		final CloudModel.Shape shape = model.getShape();
		if (shape != null) {
			cloud.setAttribute("SHAPE", shape.toString());
		}
		//		final int width = model.getWidth();
		//		if (width != CloudController.DEFAULT_WIDTH) {
		//			cloud.setAttribute("WIDTH", Integer.toString(width));
		//		}
		writer.addElement(model, cloud);
	}
}
