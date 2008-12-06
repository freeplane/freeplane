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
package org.freeplane.map.link;

import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.freeplane.extension.IExtension;
import org.freeplane.io.IAttributeHandler;
import org.freeplane.io.IAttributeWriter;
import org.freeplane.io.INodeCreator;
import org.freeplane.io.INodeWriter;
import org.freeplane.io.IReadCompletionListener;
import org.freeplane.io.ITreeWriter;
import org.freeplane.io.ReadManager;
import org.freeplane.io.WriteManager;
import org.freeplane.io.xml.n3.nanoxml.XMLElement;
import org.freeplane.main.Tools;
import org.freeplane.map.tree.NodeBuilder;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.NodeBuilder.NodeObject;

class LinkBuilder implements INodeCreator, IAttributeHandler,
        IReadCompletionListener, INodeWriter<IExtension>,
        IAttributeWriter<IExtension> {
	final private HashSet<ArrowLinkModel> arrowLinks;

	public LinkBuilder() {
		arrowLinks = new HashSet<ArrowLinkModel>();
	}

	public void completeNode(final Object parent, final String tag,
	                         final Object userObject) {
		if (parent instanceof NodeObject) {
			final NodeModel node = ((NodeObject) parent).node;
			if (userObject instanceof ArrowLinkModel) {
				final ArrowLinkModel arrowLink = (ArrowLinkModel) userObject;
				arrowLink.setSource(node);
			}
			return;
		}
	}

	protected ArrowLinkModel createArrowLink(final NodeModel source,
	                                         final String targetID) {
		return new ArrowLinkModel(source, targetID);
	}

	public Object createNode(final Object parent, final String tag) {
		if (tag.equals("arrowlink")) {
			return createArrowLink(null, null);
		}
		return null;
	}

	public boolean parseAttribute(final Object userObject, final String tag,
	                              final String name, final String value) {
		if (tag.equals(NodeBuilder.XML_NODE)
		        && userObject instanceof NodeObject) {
			if (name.equals("LINK")) {
				final NodeModel node = ((NodeObject) userObject).node;
				(node.getModeController().getLinkController()).loadLink(node,
				    value);
				return true;
			}
		}
		if (userObject instanceof ArrowLinkModel) {
			final ArrowLinkModel arrowLink = (ArrowLinkModel) userObject;
			if (name.equals("STYLE")) {
				arrowLink.setStyle(value.toString());
			}
			else if (name.equals("COLOR")) {
				arrowLink.setColor(Tools.xmlToColor(value.toString()));
			}
			else if (name.equals("DESTINATION")) {
				arrowLink.setTargetID(value);
				arrowLinks.add(arrowLink);
			}
			else if (name.equals("REFERENCETEXT")) {
				arrowLink.setReferenceText((value.toString()));
			}
			else if (name.equals("STARTINCLINATION")) {
				arrowLink.setStartInclination(Tools
				    .xmlToPoint(value.toString()));
			}
			else if (name.equals("ENDINCLINATION")) {
				arrowLink.setEndInclination(Tools.xmlToPoint(value.toString()));
			}
			else if (name.equals("STARTARROW")) {
				arrowLink.setStartArrow(value.toString());
			}
			else if (name.equals("ENDARROW")) {
				arrowLink.setEndArrow(value.toString());
			}
			else if (name.equals("WIDTH")) {
				arrowLink.setWidth(Integer.parseInt(value.toString()));
			}
			return true;
		}
		return false;
	}

	/**
	 * Completes the links within the getMap(). They are registered in the
	 * registry.
	 */
	public void readingCompleted(final NodeModel topNode,
	                             final HashMap<String, String> newIds) {
		final Iterator<ArrowLinkModel> iterator = arrowLinks.iterator();
		while (iterator.hasNext()) {
			final ArrowLinkModel arrowLink = iterator.next();
			final String id = arrowLink.getTargetID();
			final String newId = newIds.get(id);
			final String targetID = newId != null ? newId : id;
			if (targetID == null) {
				new NullPointerException().printStackTrace();
				continue;
			}
			arrowLink.setTargetID(targetID);
			final NodeModel source = arrowLink.getSource();
			NodeLinks.createLinkExtension(source).addArrowlink(arrowLink);
		}
		arrowLinks.clear();
	}

	/**
	 */
	public void registerBy(final ReadManager reader, final WriteManager writer) {
		reader.addNodeCreator("arrowlink", this);
		reader.addAttributeHandler("node", this);
		reader.addReadCompletionListener(this);
		writer.addExtensionAttributeWriter(NodeLinks.class, this);
		writer.addExtensionNodeWriter(NodeLinks.class, this);
	}

	public XMLElement save(final ArrowLinkModel model) {
		final XMLElement arrowLink = new XMLElement();
		arrowLink.setName("arrowlink");
		final String style = model.getStyle();
		if (style != null) {
			arrowLink.setAttribute("STYLE", style);
		}
		final Color color = model.getColor();
		if (color != null) {
			arrowLink.setAttribute("COLOR", Tools.colorToXml(color));
		}
		final String destinationLabel = model.getTarget().createID();
		if (destinationLabel != null) {
			arrowLink.setAttribute("DESTINATION", destinationLabel);
		}
		final String referenceText = model.getReferenceText();
		if (referenceText != null) {
			arrowLink.setAttribute("REFERENCETEXT", referenceText);
		}
		final Point startInclination = model.getStartInclination();
		if (startInclination != null) {
			arrowLink.setAttribute("STARTINCLINATION", Tools
			    .PointToXml(startInclination));
		}
		final Point endInclination = model.getEndInclination();
		if (endInclination != null) {
			arrowLink.setAttribute("ENDINCLINATION", Tools
			    .PointToXml(endInclination));
		}
		final String startArrow = model.getStartArrow();
		if (startArrow != null) {
			arrowLink.setAttribute("STARTARROW", startArrow);
		}
		final String endArrow = model.getEndArrow();
		if (endArrow != null) {
			arrowLink.setAttribute("ENDARROW", endArrow);
		}
		return arrowLink;
	}

	public void writeAttributes(final ITreeWriter writer,
	                            final Object userObject,
	                            final IExtension extension) {
		final NodeLinks links = (NodeLinks) extension;
		final String link = links.getLink();
		if (link != null) {
			writer.addAttribute("LINK", link);
		}
	}

	public void writeContent(final ITreeWriter writer, final Object node,
	                         final IExtension extension) throws IOException {
		final NodeLinks links = (NodeLinks) extension;
		final Iterator<LinkModel> iterator = links.getLinks().iterator();
		while (iterator.hasNext()) {
			final LinkModel linkModel = iterator.next();
			if (linkModel instanceof ArrowLinkModel) {
				final ArrowLinkModel arrowLinkModel = (ArrowLinkModel) linkModel;
				final XMLElement arrowLinkElement = save(arrowLinkModel);
				writer.addNode(arrowLinkModel, arrowLinkElement);
			}
		}
	}
}
