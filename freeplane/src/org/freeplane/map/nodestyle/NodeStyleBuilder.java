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

import java.awt.Font;
import java.io.IOException;

import org.freeplane.extension.IExtension;
import org.freeplane.io.IAttributeHandler;
import org.freeplane.io.IAttributeWriter;
import org.freeplane.io.INodeCreator;
import org.freeplane.io.INodeWriter;
import org.freeplane.io.ITreeWriter;
import org.freeplane.io.ReadManager;
import org.freeplane.io.WriteManager;
import org.freeplane.io.xml.n3.nanoxml.IXMLElement;
import org.freeplane.io.xml.n3.nanoxml.XMLElement;
import org.freeplane.main.Tools;
import org.freeplane.map.tree.NodeBuilder;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.NodeBuilder.NodeObject;

public class NodeStyleBuilder implements INodeCreator, IAttributeHandler, INodeWriter<IExtension>,
        IAttributeWriter<IExtension> {
	static class FontProperties {
		String fontName;
		int fontSize = 0;
		int fontStyle = 0;
	}

	public NodeStyleBuilder() {
	}

	public void completeNode(final Object parent, final String tag, final Object userObject) {
		if (parent instanceof NodeObject) {
			final NodeModel node = ((NodeObject) parent).node;
			if (tag.equals("font")) {
				final FontProperties fp = (FontProperties) userObject;
				final Font font = new Font(fp.fontName, fp.fontStyle, fp.fontSize);
				node.setFont(font);
				return;
			}
			return;
		}
	}

	public Object createNode(final Object parent, final String tag) {
		if (tag.equals("font")) {
			return new FontProperties();
		}
		return null;
	}

	public boolean parseAttribute(final Object userObject, final String tag, final String name,
	                              final String value) {
		if (tag.equals(NodeBuilder.XML_NODE) && userObject instanceof NodeObject) {
			final NodeModel node = ((NodeObject) userObject).node;
			if (name.equals("COLOR")) {
				if (value.length() == 7) {
					node.setColor(Tools.xmlToColor(value));
				}
				return true;
			}
			else if (name.equals("BACKGROUND_COLOR")) {
				if (value.length() == 7) {
					node.setBackgroundColor(Tools.xmlToColor(value));
				}
				return true;
			}
			else if (name.equals("STYLE")) {
				node.setShape(value);
				return true;
			}
			return false;
		}
		if (tag.equals("font")) {
			final FontProperties fp = (FontProperties) userObject;
			if (name.equals("SIZE")) {
				fp.fontSize = Integer.parseInt(value.toString());
			}
			else if (name.equals("NAME")) {
				fp.fontName = value.toString();
			}
			else if (value.toString().equals("true")) {
				if (name.equals("BOLD")) {
					fp.fontStyle += Font.BOLD;
				}
				else if (name.equals("ITALIC")) {
					fp.fontStyle += Font.ITALIC;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 */
	public void registerBy(final ReadManager reader, final WriteManager writer) {
		reader.addNodeCreator("font", this);
		reader.addAttributeHandler("node", this);
		writer.addExtensionNodeWriter(NodeStyleModel.class, this);
		writer.addExtensionAttributeWriter(NodeStyleModel.class, this);
	}

	public void setAttributes(final String tag, final Object node, final IXMLElement attributes) {
	}

	public void writeAttributes(final ITreeWriter writer, final Object userObject,
	                            final IExtension extension) {
		final NodeStyleModel style = (NodeStyleModel) extension;
		if (style.getColor() != null) {
			writer.addAttribute("COLOR", Tools.colorToXml(style.getColor()));
		}
		if (style.getBackgroundColor() != null) {
			writer.addAttribute("BACKGROUND_COLOR", Tools.colorToXml(style.getBackgroundColor()));
		}
		if (style.getShape() != null) {
			writer.addAttribute("STYLE", style.getShape());
		}
	}

	public void writeContent(final ITreeWriter writer, final Object node, final IExtension extension)
	        throws IOException {
		final NodeStyleModel style = (NodeStyleModel) extension;
		final Font font = style.getFont();
		if (font != null) {
			final XMLElement fontElement = new XMLElement();
			fontElement.setName("font");
			fontElement.setAttribute("NAME", font.getFamily());
			if (font.getSize() != 0) {
				fontElement.setAttribute("SIZE", Integer.toString(font.getSize()));
			}
			if (font.isBold()) {
				fontElement.setAttribute("BOLD", "true");
			}
			if (font.isItalic()) {
				fontElement.setAttribute("ITALIC", "true");
			}
			writer.addNode(font, fontElement);
		}
	}
}
