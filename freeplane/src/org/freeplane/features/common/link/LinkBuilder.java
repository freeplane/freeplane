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
package org.freeplane.features.common.link;

import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IAttributeWriter;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.IExtensionAttributeWriter;
import org.freeplane.core.io.IExtensionElementWriter;
import org.freeplane.core.io.IReadCompletionListener;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.NodeBuilder;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.io.xml.TreeXmlWriter;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.n3.nanoxml.XMLElement;

class LinkBuilder implements IElementDOMHandler, IReadCompletionListener, IExtensionElementWriter,
        IExtensionAttributeWriter, IAttributeWriter {
	final private HashSet<ArrowLinkModel> arrowLinks;
	private final LinkController linkController;

	public LinkBuilder(final LinkController linkController) {
		this.linkController = linkController;
		arrowLinks = new HashSet<ArrowLinkModel>();
	}

	protected ArrowLinkModel createArrowLink(final NodeModel source, final String targetID) {
		return new ArrowLinkModel(source, targetID);
	}

	public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
		if (tag.equals("arrowlink")) {
			return createArrowLink(null, null);
		}
		return null;
	}

	public void endElement(final Object parent, final String tag, final Object userObject, final XMLElement dom) {
		if (parent instanceof NodeModel) {
			final NodeModel node = (NodeModel) parent;
			if (userObject instanceof ArrowLinkModel) {
				final ArrowLinkModel arrowLink = (ArrowLinkModel) userObject;
				arrowLink.setSource(node);
			}
			return;
		}
	}

	/**
	 * Completes the links within the getMap(). They are registered in the
	 * registry.
	 */
	public void readingCompleted(final NodeModel topNode, final HashMap<String, String> newIds) {
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

	private void registerAttributeHandlers(final ReadManager reader) {
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "LINK", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				linkController.loadLink(node, value);
			}
		});
		reader.addAttributeHandler("arrowlink", "STYLE", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ArrowLinkModel arrowLink = (ArrowLinkModel) userObject;
				arrowLink.setStyle(value.toString());
			}
		});
		reader.addAttributeHandler("arrowlink", "COLOR", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ArrowLinkModel arrowLink = (ArrowLinkModel) userObject;
				arrowLink.setColor(ColorUtils.stringToColor(value.toString()));
			}
		});
		reader.addAttributeHandler("arrowlink", "DESTINATION", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ArrowLinkModel arrowLink = (ArrowLinkModel) userObject;
				arrowLink.setTargetID(value);
				arrowLinks.add(arrowLink);
			}
		});
		reader.addAttributeHandler("arrowlink", "REFERENCETEXT", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ArrowLinkModel arrowLink = (ArrowLinkModel) userObject;
				arrowLink.setReferenceText((value.toString()));
			}
		});
		reader.addAttributeHandler("arrowlink", "STARTINCLINATION", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ArrowLinkModel arrowLink = (ArrowLinkModel) userObject;
				arrowLink.setStartInclination(TreeXmlReader.xmlToPoint(value.toString()));
			}
		});
		reader.addAttributeHandler("arrowlink", "ENDINCLINATION", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ArrowLinkModel arrowLink = (ArrowLinkModel) userObject;
				arrowLink.setEndInclination(TreeXmlReader.xmlToPoint(value.toString()));
			}
		});
		reader.addAttributeHandler("arrowlink", "STARTARROW", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ArrowLinkModel arrowLink = (ArrowLinkModel) userObject;
				arrowLink.setStartArrow(value.toString());
			}
		});
		reader.addAttributeHandler("arrowlink", "ENDARROW", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ArrowLinkModel arrowLink = (ArrowLinkModel) userObject;
				arrowLink.setEndArrow(value.toString());
			}
		});
		reader.addAttributeHandler("arrowlink", "WIDTH", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ArrowLinkModel arrowLink = (ArrowLinkModel) userObject;
				arrowLink.setWidth(Integer.parseInt(value.toString()));
			}
		});
	}

	/**
	 */
	public void registerBy(final ReadManager reader, final WriteManager writer) {
		reader.addElementHandler("arrowlink", this);
		registerAttributeHandlers(reader);
		reader.addReadCompletionListener(this);
		writer.addExtensionAttributeWriter(NodeLinks.class, this);
		writer.addExtensionElementWriter(NodeLinks.class, this);
		writer.addAttributeWriter(NodeBuilder.XML_NODE, this);
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
			arrowLink.setAttribute("COLOR", ColorUtils.colorToString(color));
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
			arrowLink.setAttribute("STARTINCLINATION", TreeXmlWriter.PointToXml(startInclination));
		}
		final Point endInclination = model.getEndInclination();
		if (endInclination != null) {
			arrowLink.setAttribute("ENDINCLINATION", TreeXmlWriter.PointToXml(endInclination));
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

	public void setAttributes(final String tag, final Object node, final XMLElement attributes) {
	}

	public void writeAttributes(final ITreeWriter writer, final Object userObject, final IExtension extension) {
		final NodeLinks links = (NodeLinks) extension;
		final String link = links.getHyperLink();
		if (link != null) {
			writer.addAttribute("LINK", link);
		}
	}

	public void writeAttributes(final ITreeWriter writer, final Object userObject, final String tag) {
		final NodeModel node = (NodeModel) userObject;
		final boolean saveID = MapController.saveOnlyIntrinsicallyNeededIds()
		        && !linkController.getLinksTo(node).isEmpty();
		if (saveID) {
			final String id = node.createID();
			writer.addAttribute("ID", id);
		}
	}

	public void writeContent(final ITreeWriter writer, final Object node, final IExtension extension)
	        throws IOException {
		final NodeLinks links = (NodeLinks) extension;
		final Iterator<LinkModel> iterator = links.getLinks().iterator();
		while (iterator.hasNext()) {
			final LinkModel linkModel = iterator.next();
			if (linkModel instanceof ArrowLinkModel) {
				final ArrowLinkModel arrowLinkModel = (ArrowLinkModel) linkModel;
				final XMLElement arrowLinkElement = save(arrowLinkModel);
				writer.addElement(arrowLinkModel, arrowLinkElement);
			}
		}
	}
}
