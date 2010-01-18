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
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IAttributeWriter;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.IElementHandler;
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

class LinkBuilder implements IElementHandler, IReadCompletionListener, IExtensionElementWriter,
        IExtensionAttributeWriter, IAttributeWriter {
	final private HashSet<NodeLinkModel> arrowLinks;
	private final LinkController linkController;

	public LinkBuilder(final LinkController linkController) {
		this.linkController = linkController;
		arrowLinks = new HashSet<NodeLinkModel>();
	}

	protected NodeLinkModel createArrowLink(final NodeModel source, final String targetID) {
		return new ConnectorModel(source, targetID);
	}

	public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
		if (tag.equals("arrowlink")) {
			return createArrowLink((NodeModel)parent, null);
		}
		return null;
	}

	/**
	 * Completes the links within the getMap(). They are registered in the
	 * registry.
	 */
	public void readingCompleted(final NodeModel topNode, final HashMap<String, String> newIds) {
		final Iterator<NodeLinkModel> iterator = arrowLinks.iterator();
		while (iterator.hasNext()) {
			final NodeLinkModel arrowLink = iterator.next();
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
		reader.addAttributeHandler("arrowlink", "EDGE_LIKE", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setEdgeLike(Boolean.TRUE.toString().equals(value));
			}
		});
		reader.addAttributeHandler("arrowlink", "COLOR", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setColor(ColorUtils.stringToColor(value.toString()));
			}
		});
		reader.addAttributeHandler("arrowlink", "DESTINATION", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setTargetID(value);
				arrowLinks.add(arrowLink);
			}
		});
		reader.addAttributeHandler("arrowlink", "SOURCE_LABEL", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setSourceLabel(value.toString());
			}
		});
		reader.addAttributeHandler("arrowlink", "MIDDLE_LABEL", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setMiddleLabel(value.toString());
			}
		});
		reader.addAttributeHandler("arrowlink", "TARGET_LABEL", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setTargetLabel(value.toString());
			}
		});
		reader.addAttributeHandler("arrowlink", "STARTINCLINATION", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setStartInclination(TreeXmlReader.xmlToPoint(value.toString()));
			}
		});
		reader.addAttributeHandler("arrowlink", "ENDINCLINATION", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setEndInclination(TreeXmlReader.xmlToPoint(value.toString()));
			}
		});
		reader.addAttributeHandler("arrowlink", "STARTARROW", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setStartArrow(ArrowType.valueOf(value.toUpperCase()));
			}
		});
		reader.addAttributeHandler("arrowlink", "ENDARROW", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setEndArrow(ArrowType.valueOf(value.toUpperCase()));
			}
		});
		reader.addAttributeHandler("arrowlink", "WIDTH", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
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

	public void save(final ITreeWriter writer, final ConnectorModel model) throws IOException {
		final NodeModel target = model.getTarget();
		if (target == null) {
			return;
		}
		final XMLElement arrowLink = new XMLElement();
		arrowLink.setName("arrowlink");
		final boolean isEdgeLike = model.isEdgeLike();
		if (isEdgeLike ) {
			arrowLink.setAttribute("EDGE_LIKE", Boolean.TRUE.toString());
		}
		final Color color = model.getColor();
		if (color != null) {
			arrowLink.setAttribute("COLOR", ColorUtils.colorToString(color));
		}
		final String destinationLabel = target.createID();
		if (destinationLabel != null) {
			arrowLink.setAttribute("DESTINATION", destinationLabel);
		}
		final String sourceLabel = model.getSourceLabel();
		if (sourceLabel != null) {
			arrowLink.setAttribute("SOURCE_LABEL", sourceLabel);
		}
		final String targetLabel = model.getTargetLabel();
		if (targetLabel != null) {
			arrowLink.setAttribute("TARGET_LABEL", targetLabel);
		}
		final String middleLabel = model.getMiddleLabel();
		if (middleLabel != null) {
			arrowLink.setAttribute("MIDDLE_LABEL", middleLabel);
		}
		final Point startInclination = model.getStartInclination();
		if (startInclination != null) {
			arrowLink.setAttribute("STARTINCLINATION", TreeXmlWriter.PointToXml(startInclination));
		}
		final Point endInclination = model.getEndInclination();
		if (endInclination != null) {
			arrowLink.setAttribute("ENDINCLINATION", TreeXmlWriter.PointToXml(endInclination));
		}
		final String startArrow = model.getStartArrow().toString();
		if (startArrow != null) {
			arrowLink.setAttribute("STARTARROW", startArrow);
		}
		final String endArrow = model.getEndArrow().toString();
		if (endArrow != null) {
			arrowLink.setAttribute("ENDARROW", endArrow);
		}
		writer.addElement(model, arrowLink);
	}

	public void setAttributes(final String tag, final Object node, final XMLElement attributes) {
	}

	public void writeAttributes(final ITreeWriter writer, final Object userObject, final IExtension extension) {
		final NodeLinks links = (NodeLinks) extension;
		final URI link = links.getHyperLink();
		if (link != null) {
			final String string = link.toString();
			if(string.startsWith("#")){
				if(((NodeModel)userObject).getMap().getNodeForID(string.substring(1)) == null){
					return;
				}
			}
			writer.addAttribute("LINK", string);
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
			if (linkModel instanceof ConnectorModel) {
				final ConnectorModel arrowLinkModel = (ConnectorModel) linkModel;
				save(writer, arrowLinkModel);
			}
		}
	}
}
