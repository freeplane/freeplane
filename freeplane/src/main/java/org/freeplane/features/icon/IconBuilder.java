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
package org.freeplane.features.icon;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.IElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapWriter;
import org.freeplane.features.map.NodeBuilder;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeWriter;
import org.freeplane.n3.nanoxml.XMLElement;

class IconBuilder implements IElementDOMHandler, IElementWriter {
	private final IconStore store;

	public IconBuilder(final IconController iconController, final IconStore icons) {
		store = icons;
	}

	static class IconProperties {
		String iconName;
	}

	@Override
	public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
		if (tag.equals("icon")) {
			return new IconProperties();
		}
		return null;
	}

	@Override
	public void endElement(final Object parent, final String tag, final Object userObject, final XMLElement dom) {
		if (parent instanceof NodeModel && tag.equals("icon")) {
			final NodeModel node = (NodeModel) parent;
			final IconProperties ip = (IconProperties) userObject;
			final String iconName = ip.iconName;
			if (iconName != null)
				node.addIcon(store.getMindIcon(iconName));
			return;
		}
	}

	private void registerAttributeHandlers(final ReadManager reader) {
		reader.addAttributeHandler("icon", "BUILTIN", new IAttributeHandler() {
			@Override
			public void setAttribute(final Object userObject, final String value) {
				final IconProperties ip = (IconProperties) userObject;
				ip.iconName = value;
			}
		});
	}

	/**
	 */
	public void registerBy(final ReadManager reader, final WriteManager writer) {
		reader.addElementHandler("icon", this);
		registerAttributeHandlers(reader);
		writer.addElementWriter(NodeBuilder.XML_NODE, this);
		writer.addElementWriter(NodeBuilder.XML_STYLENODE, this);
	}

	public void setAttributes(final String tag, final Object node, final XMLElement attributes) {
	}

	@Override
	public void writeContent(final ITreeWriter writer, final Object element, final String tag) throws IOException {
		if (!NodeWriter.shouldWriteSharedContent(writer))
			return;
		final boolean forceFormatting = Boolean.TRUE.equals(writer.getHint(MapWriter.WriterHint.FORCE_FORMATTING));
		final NodeModel node = (NodeModel) element;
		final IconController iconController = IconController.getController();
		final Collection<NamedIcon> icons = forceFormatting ? iconController.getIcons(node)
		        : node.getIcons();
		for (NamedIcon icon : icons) {
			final XMLElement iconElement = new XMLElement();
			iconElement.setName("icon");
			iconElement.setAttribute("BUILTIN", icon.getName());
			if (forceFormatting) {
			    MindIcon mindIcon = (MindIcon) icon;
			    String iconFile;
                try {
                    iconFile = new URI(null, mindIcon.getFile(), null).toString();
                    iconElement.setAttribute("src", iconFile);
                    iconElement.setAttribute("height",
                            Integer.toString(iconController.getIconSize(node).toBaseUnitsRounded()));
                } catch (Exception e) {
                    LogUtils.severe(e);
                }
			}
			writer.addElement(node, iconElement);
		}
	}
}
