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

import java.awt.Point;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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

	private NodeLinkModel createArrowLink(final NodeModel source, final String targetID) {
		ConnectorModel connectorModel = new ConnectorModel(source, targetID);
        return connectorModel;
	}

	@Override
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
	@Override
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
			@Override
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				linkController.loadLink(node, value);
				final Collection<NodeLinkModel> links = NodeLinks.getLinks(node);
				processedLinks.addAll(links);
			}
		});

		final IAttributeHandler hyperlinkHandler = new IAttributeHandler() {
			@Override
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				linkController.loadLinkFormat(node, Boolean.parseBoolean(value));
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, FORMAT_AS_HYPERLINK, hyperlinkHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, FORMAT_AS_HYPERLINK, hyperlinkHandler);

		reader.addAttributeHandler("arrowlink", "EDGE_LIKE", new IAttributeHandler() {
			@Override
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setShape(Optional.of(Shape.EDGE_LIKE));
			}
		});
		reader.addAttributeHandler("arrowlink", "SHAPE", new IAttributeHandler() {
			@Override
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setShape(Optional.of(Shape.valueOf(value)));
			}
		});
		reader.addAttributeHandler("arrowlink", "DASH", new IAttributeHandler() {
			@Override
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				final String[] split = value.isEmpty() ? new String[] {} : value.split(" ");
				int[] dash = new int[split.length];
				int i = 0;
				for(String s : split){
					dash[i++] = Integer.parseInt(s);
				}
				arrowLink.setDash(Optional.of(dash));
			}
		});
		reader.addAttributeHandler("arrowlink", "DESTINATION", new IAttributeHandler() {
			@Override
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setTargetID(value);
				processedLinks.add(arrowLink);
			}
		});
		reader.addAttributeHandler("arrowlink", "SOURCE_LABEL", new IAttributeHandler() {
			@Override
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setSourceLabel(value.toString());
			}
		});
		reader.addAttributeHandler("arrowlink", "MIDDLE_LABEL", new IAttributeHandler() {
			@Override
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setMiddleLabel(value.toString());
			}
		});
		reader.addAttributeHandler("arrowlink", "TARGET_LABEL", new IAttributeHandler() {
			@Override
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setTargetLabel(value.toString());
			}
		});
		reader.addAttributeHandler("arrowlink", "STARTINCLINATION", new IAttributeHandler() {
			@Override
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setStartInclination(TreeXmlReader.xmlToPoint(value.toString()));
			}
		});
		reader.addAttributeHandler("arrowlink", "ENDINCLINATION", new IAttributeHandler() {
			@Override
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setEndInclination(TreeXmlReader.xmlToPoint(value.toString()));
			}
		});
		reader.addAttributeHandler("arrowlink", "STARTARROW", new IAttributeHandler() {
			@Override
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setStartArrow(Optional.of(ArrowType.valueOf(value.toUpperCase(Locale.ENGLISH))));
			}
		});
		reader.addAttributeHandler("arrowlink", "ENDARROW", new IAttributeHandler() {
			@Override
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setEndArrow(Optional.of(ArrowType.valueOf(value.toUpperCase(Locale.ENGLISH))));
			}
		});
		reader.addAttributeHandler("arrowlink", "WIDTH", new IAttributeHandler() {
			@Override
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setWidth(Optional.of(Integer.parseInt(value.toString())));
			}
		});

		reader.addAttributeHandler("arrowlink", "FONT_FAMILY", new IAttributeHandler() {
			@Override
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setLabelFontFamily(Optional.of(value.toString()));
			}
		});
		reader.addAttributeHandler("arrowlink", "FONT_SIZE", new IAttributeHandler() {
			@Override
			public void setAttribute(final Object userObject, final String value) {
				final ConnectorModel arrowLink = (ConnectorModel) userObject;
				arrowLink.setLabelFontSize(Optional.of(Integer.parseInt(value.toString())));
			}
		});
	}

	@Override
	public void endElement(Object parent, String tag, Object element, XMLElement dom) {
		final ConnectorModel arrowLink = (ConnectorModel) element;
		final String color = dom.getAttribute("COLOR", null);
		final String transparency = dom.getAttribute("TRANSPARENCY", null);
		if(color != null){
			arrowLink.setColor(Optional.of(ColorUtils.stringToColor(color)));
			if(transparency == null){
				arrowLink.setAlpha(Optional.of(ColorUtils.NON_TRANSPARENT_ALPHA));
			}
		}

		if(transparency != null){
			arrowLink.setAlpha(Optional.of(Integer.parseInt(transparency)));
		}
		fixSelfLoopedConnectorShape(arrowLink);
	}

	private void fixSelfLoopedConnectorShape(ConnectorModel connector) {
		if (connector.isSelfLink()
				&& Shape.CUBIC_CURVE.equals(connector.getShape())
				&& MapVersionInterpreter.isOlderThan(connector.getSource().getMap(), FREEPLANE_VERSION_WITH_CURVED_LOOPED_CONNECTORS))
			connector.setShape(Optional.of(Shape.LINE));
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
		model.getShape().ifPresent( shape ->
		arrowLink.setAttribute("SHAPE", shape.name()));
        model.getColor().ifPresent( color ->
		arrowLink.setAttribute("COLOR", ColorUtils.colorToString(color)));
		model.getWidth().ifPresent( width ->
		arrowLink.setAttribute("WIDTH", Integer.toString(width)));
        model.getAlpha().ifPresent( alpha ->
		arrowLink.setAttribute("TRANSPARENCY", Integer.toString(alpha)));
        model.getDash().ifPresent( dash -> {
                StringBuilder sb = new StringBuilder(dash.length * 4);
                for(int i : dash){
                    if(sb.length() > 0){
                        sb.append(' ');
                    }
                    sb.append(i);
                }
                arrowLink.setAttribute("DASH", sb.toString());
		});

        model.getLabelFontSize().ifPresent( fontSize ->
		arrowLink.setAttribute("FONT_SIZE", Integer.toString(fontSize)));

        model.getLabelFontFamily().ifPresent( fontFamily ->
		arrowLink.setAttribute("FONT_FAMILY", fontFamily));

		final String destinationId = target.createID();

		if (destinationId != null) {
		    arrowLink.setAttribute("DESTINATION", destinationId);
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
        model.getStartArrow().ifPresent( startArrow -> 
        arrowLink.setAttribute("STARTARROW", startArrow.name()));
        model.getEndArrow().ifPresent( endArrow -> 
        arrowLink.setAttribute("STARTARROW", endArrow.name()));
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
				if (string.startsWith("#ID")) {
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
						final ConnectorModel arrowLinkModel = (ConnectorModel) linkModel.cloneForSource(node);
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
