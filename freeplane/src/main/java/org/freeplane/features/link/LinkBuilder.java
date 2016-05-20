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
package org.freeplane.features.link;

import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.IReadCompletionListener;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.io.xml.TreeXmlWriter;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.link.ConnectorModel.Shape;
import org.freeplane.features.map.NodeBuilder;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.url.MapVersionInterpreter;
import org.freeplane.n3.nanoxml.XMLElement;

public class LinkBuilder implements IElementDOMHandler, IReadCompletionListener{
	private static final int FREEPLANE_VERSION_WITH_CURVED_LOOPED_CONNECTORS = 3;
	private static final String FORMAT_AS_HYPERLINK = "FORMAT_AS_HYPERLINK";
	private static final String LINK = "LINK";
	final private HashSet<NodeLinkModel> processedLinks;
	private final LinkController linkController;

	public LinkBuilder(final LinkController linkController) {
		this.linkController = linkController;
		processedLinks = new HashSet<NodeLinkModel>();
	}

	protected NodeLinkModel createArrowLink(final NodeModel source, final String targetID) {
		return new ConnectorModel(source, targetID, 
			linkController.getStandardConnectorColor(),
			linkController.getStandardConnectorAlpha(),
			linkController.getStandardConnectorShape(),
		    linkController.getStandardConnectorWidth(),
		    linkController.getStandardLabelFontFamily(), 
		    linkController.getStandardLabelFontSize());
	}

	public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
		if (tag.equals("arrowlink")) {
			return createArrowLink((NodeModel) parent, null);
		}
		return null;
	}

	/**
	 * Completes the links within the getMap(). They are registered in the
	 * registry.
	 */
	public void readingCompleted(final NodeModel topNode, final Map<String, String> newIds) {
		final Iterator<NodeLinkModel> iterator = processedLinks.iterator();
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
		processedLinks.clear();
	}

	private void registerAttributeHandlers(final ReadManager reader) {
		reader.addAttributeHandler(NodeBuilder.XML_NODE, LINK, new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				linkController.loadLink(node, value);
				final Collection<NodeLinkModel> links = NodeLinks.getLinks(node);
				processedLinks.addAll(links);
			}
		});
		
		final IAttributeHandler hyperlinkHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				linkController.loadLinkFormat(node, Boolean.parseBoolean(value));
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, FORMAT_AS_HYPERLINK, hyperlinkHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, FORMAT_AS_HYPERLINK, hyperlinkHandler);
		
		reader.addAttributeHandler("arrowlink", "EDGE_LIKE", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setShape(Shape.EDGE_LIKE);
			}
		});
		reader.addAttributeHandler("arrowlink", "SHAPE", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setShape(Shape.valueOf(value));
			}
		});
		reader.addAttributeHandler("arrowlink", "DASH", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				final String[] split = value.split(" ");
				int[] dash = new int[split.length];
				int i = 0;
				for(String s : split){
					dash[i++] = Integer.parseInt(s);
				}
				arrowLink.setDash(dash);
			}
		});
		reader.addAttributeHandler("arrowlink", "DESTINATION", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setTargetID(value);
				processedLinks.add(arrowLink);
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
				arrowLink.setStartArrow(ArrowType.valueOf(value.toUpperCase(Locale.ENGLISH)));
			}
		});
		reader.addAttributeHandler("arrowlink", "ENDARROW", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setEndArrow(ArrowType.valueOf(value.toUpperCase(Locale.ENGLISH)));
			}
		});
		reader.addAttributeHandler("arrowlink", "WIDTH", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setWidth(Integer.parseInt(value.toString()));
			}
		});
		
		reader.addAttributeHandler("arrowlink", "FONT_FAMILY", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setLabelFontFamily(value.toString());
			}
		});
		reader.addAttributeHandler("arrowlink", "FONT_SIZE", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setLabelFontSize(Integer.parseInt(value.toString()));
			}
		});
	}

	public void endElement(Object parent, String tag, Object element, XMLElement dom) {
		final ConnectorModel arrowLink = (ConnectorModel) element;
		final String color = dom.getAttribute("COLOR", null);
		final String transparency = dom.getAttribute("TRANSPARENCY", null);
		if(color != null){
			arrowLink.setColor(ColorUtils.stringToColor(color));
			if(transparency == null){
				arrowLink.setAlpha(255);
			}
		}
		else{
			arrowLink.setColor(linkController.getStandardConnectorColor());
		}

		if(transparency != null){
			arrowLink.setAlpha(Integer.parseInt(transparency));
		}
		else if(color == null){
			arrowLink.setAlpha(linkController.getStandardConnectorAlpha());	
		}
		fixSelfLoopedConnectorShape(arrowLink);
	}

	private void fixSelfLoopedConnectorShape(ConnectorModel connector) {
		if (connector.isSelfLink() 
				&& Shape.CUBIC_CURVE.equals(connector.getShape()) 
				&& MapVersionInterpreter.isOlderThan(connector.getSource().getMap(), FREEPLANE_VERSION_WITH_CURVED_LOOPED_CONNECTORS))
			connector.setShape(Shape.LINE);
	}

	void registerBy(final ReadManager reader) {
		reader.addElementHandler("arrowlink", this);
		registerAttributeHandlers(reader);
		reader.addReadCompletionListener(this);
	}

	public void save(final ITreeWriter writer, final ConnectorModel model) throws IOException {
		final NodeModel target = model.getTarget();
		if (target == null) {
			return;
		}
		final XMLElement arrowLink = new XMLElement();
		arrowLink.setName("arrowlink");
		final Shape shape = model.getShape();
		arrowLink.setAttribute("SHAPE", shape.toString());
		final Color color = model.getColor();
		arrowLink.setAttribute("COLOR", ColorUtils.colorToString(color));
		final int width = model.getWidth();
		arrowLink.setAttribute("WIDTH", Integer.toString(width));
		final int alpha = model.getAlpha();
		arrowLink.setAttribute("TRANSPARENCY", Integer.toString(alpha));
		final int[]dash = model.getDash();
		if (dash != null) {
			StringBuilder sb = null;
			for(int i : dash){
				if(sb == null){
					sb = new StringBuilder(dash.length * 4);
				}
				else{
					sb.append(' ');
				}
				sb.append(i);
			}
			if(sb != null){
				arrowLink.setAttribute("DASH", sb.toString());				
			}
		}
		
		final int fontSize = model.getLabelFontSize();
		arrowLink.setAttribute("FONT_SIZE", Integer.toString(fontSize));

		final String fontFamily = model.getLabelFontFamily();
		arrowLink.setAttribute("FONT_FAMILY", fontFamily);
		
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

	public void writeAttributes(final ITreeWriter writer, final NodeModel node) {
		final NodeLinks links = node.getExtension(NodeLinks.class);
		if(links != null) { 
			final URI link = links.getHyperLink(node);
			if (link != null) {
				final String string = link.toString();
				if (string.startsWith("#")) {
					if ((node).getMap().getNodeForID(string.substring(1)) == null) {
						return;
					}
				}
				writer.addAttribute(LINK, string);
			}
			final Boolean formatNodeAsHyperlink = links.formatNodeAsHyperlink();
			if (formatNodeAsHyperlink != null) {
				writer.addAttribute(FORMAT_AS_HYPERLINK, formatNodeAsHyperlink.toString());
			}
		}
	}

	public void writeContent(final ITreeWriter writer, final NodeModel node)
			throws IOException {
		final NodeLinks links = node.getExtension(NodeLinks.class);
		if(links != null) { 
			final Iterator<NodeLinkModel> iterator = links.getLinks().iterator();
			while (iterator.hasNext()) {
				final NodeLinkModel linkModel = iterator.next();
				if (linkModel instanceof ConnectorModel) {
					final boolean linkNotWrittenBefore = ! processedLinks.contains(linkModel);
					if(linkNotWrittenBefore) {
						final ConnectorModel arrowLinkModel = (ConnectorModel) linkModel.cloneForSource((NodeModel) node);
						if(arrowLinkModel != null) {
							save(writer, arrowLinkModel);
							processedLinks.add(linkModel);
						}
					}
				}
			}
		}
	}
}
