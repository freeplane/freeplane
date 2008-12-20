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
package org.freeplane.map.cloud;

import java.awt.Color;
import java.io.IOException;

import org.freeplane.extension.IExtension;
import org.freeplane.io.IAttributeHandler;
import org.freeplane.io.INodeCreator;
import org.freeplane.io.INodeWriter;
import org.freeplane.io.ITreeWriter;
import org.freeplane.io.ReadManager;
import org.freeplane.io.WriteManager;
import org.freeplane.io.xml.n3.nanoxml.IXMLElement;
import org.freeplane.io.xml.n3.nanoxml.XMLElement;
import org.freeplane.main.Tools;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.NodeBuilder.NodeObject;

public class CloudBuilder implements INodeCreator, IAttributeHandler, INodeWriter<IExtension> {
	public CloudBuilder() {
	}

	public void completeNode(final Object parent, final String tag, final Object userObject) {
		if (parent instanceof NodeObject) {
			final NodeModel node = ((NodeObject) parent).node;
			if (userObject instanceof CloudModel) {
				final CloudModel cloud = (CloudModel) userObject;
				node.setCloud(cloud);
			}
		}
	}

	public Object createNode(final Object parent, final String tag) {
		if (tag.equals("cloud")) {
			return new CloudModel();
		}
		return null;
	}

	public boolean parseAttribute(final Object userObject, final String tag, final String name,
	                              final String value) {
		if (userObject instanceof CloudModel) {
			final CloudModel cloud = (CloudModel) userObject;
			if (name.equals("STYLE")) {
				cloud.setStyle(value.toString());
			}
			else if (name.equals("COLOR")) {
				cloud.setColor(Tools.xmlToColor(value.toString()));
			}
			else if (name.equals("WIDTH")) {
				cloud.setWidth(Integer.parseInt(value.toString()));
			}
			return true;
		}
		return false;
	}

	/**
	 */
	public void registerBy(final ReadManager reader, final WriteManager writer) {
		reader.addNodeCreator("cloud", this);
		writer.addExtensionNodeWriter(CloudModel.class, this);
	}

	public void setAttributes(final String tag, final Object node, final IXMLElement attributes) {
	}

	public void writeContent(final ITreeWriter writer, final Object node, final IExtension extension)
	        throws IOException {
		final CloudModel model = (CloudModel) extension;
		final XMLElement cloud = new XMLElement();
		cloud.setName("cloud");
		final String style = model.getStyle();
		if (style != null) {
			cloud.setAttribute("STYLE", style);
		}
		final Color color = model.getColor();
		if (color != null) {
			cloud.setAttribute("COLOR", Tools.colorToXml(color));
		}
		final int width = model.getWidth();
		if (width != CloudController.DEFAULT_WIDTH) {
			cloud.setAttribute("WIDTH", Integer.toString(width));
		}
		writer.addNode(model, cloud);
	}
}
