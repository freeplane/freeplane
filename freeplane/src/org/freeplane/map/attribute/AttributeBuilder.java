/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.map.attribute;

import java.io.IOException;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.IExtensionElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.io.xml.n3.nanoxml.IXMLElement;
import org.freeplane.core.map.MapModel;
import org.freeplane.core.map.MapReader;
import org.freeplane.core.map.NodeModel;

class AttributeBuilder implements IElementDOMHandler, IExtensionElementWriter {
	static class AttributeProperties {
		String attributeName;
		String attributeValue;
	}

	static class RegisteredAttributeProperties {
		String attributeName;
		boolean restricted = false;
		boolean visible = false;
	}

	public static final String XML_NODE_ATTRIBUTE = "attribute";
	public static final String XML_NODE_ATTRIBUTE_LAYOUT = "attribute_layout";
	public static final String XML_NODE_ATTRIBUTE_REGISTRY = "attribute_registry";
	public static final String XML_NODE_REGISTERED_ATTRIBUTE_NAME = "attribute_name";
	public static final String XML_NODE_REGISTERED_ATTRIBUTE_VALUE = "attribute_value";
	final private MapReader mapReader;

	public AttributeBuilder(final MapReader mapReader) {
		this.mapReader = mapReader;
	}

	public Object createElement(final Object parent, final String tag, final IXMLElement attributes) {
		if (tag.equals(AttributeBuilder.XML_NODE_ATTRIBUTE)) {
			return new AttributeProperties();
		}
		if (tag.equals(AttributeBuilder.XML_NODE_REGISTERED_ATTRIBUTE_NAME)) {
			return new RegisteredAttributeProperties();
		}
		if (tag.equals(AttributeBuilder.XML_NODE_REGISTERED_ATTRIBUTE_VALUE)) {
			return parent;
		}
		return null;
	}

	public void endElement(final Object parent, final String tag, final Object userObject,
	                       final IXMLElement dom) {
		/* attributes */
		if (tag.equals(AttributeBuilder.XML_NODE_REGISTERED_ATTRIBUTE_NAME)) {
			final RegisteredAttributeProperties rap = (RegisteredAttributeProperties) userObject;
			if (rap.visible) {
				getMap().getRegistry().getAttributes().getElement(rap.attributeName).setVisibility(
				    true);
			}
			if (rap.restricted) {
				getMap().getRegistry().getAttributes().getElement(rap.attributeName)
				    .setRestriction(true);
			}
			return;
		}
		if (parent instanceof NodeModel) {
			final NodeModel node = (NodeModel) parent;
			if (tag.equals(AttributeBuilder.XML_NODE_ATTRIBUTE)) {
				final AttributeProperties ap = (AttributeProperties) userObject;
				final Attribute attribute = new Attribute(ap.attributeName, ap.attributeValue);
				node.createAttributeTableModel();
				node.getAttributes().addRowNoUndo(attribute);
				return;
			}
			return;
		}
	}

	private MapModel getMap() {
		return mapReader.getCreatedMap();
	}

	private void registerAttributeHandlers(final ReadManager reader) {
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_REGISTERED_ATTRIBUTE_NAME, "NAME",
		    new IAttributeHandler() {
			    public void setAttribute(final Object userObject, final String value) {
				    final RegisteredAttributeProperties rap = (RegisteredAttributeProperties) userObject;
				    rap.attributeName = value;
				    getMap().getRegistry().getAttributes().registry(value);
			    }
		    });
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_REGISTERED_ATTRIBUTE_NAME, "VISIBLE",
		    new IAttributeHandler() {
			    public void setAttribute(final Object userObject, final String value) {
				    final RegisteredAttributeProperties rap = (RegisteredAttributeProperties) userObject;
				    rap.visible = true;
			    }
		    });
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_REGISTERED_ATTRIBUTE_NAME,
		    "RESTRICTED", new IAttributeHandler() {
			    public void setAttribute(final Object userObject, final String value) {
				    final RegisteredAttributeProperties rap = (RegisteredAttributeProperties) userObject;
				    rap.restricted = true;
			    }
		    });
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_REGISTERED_ATTRIBUTE_VALUE, "VALUE",
		    new IAttributeHandler() {
			    public void setAttribute(final Object userObject, final String value) {
				    final RegisteredAttributeProperties rap = (RegisteredAttributeProperties) userObject;
				    final Attribute attribute = new Attribute(rap.attributeName, value);
				    final AttributeRegistry r = getMap().getRegistry().getAttributes();
				    r.registry(attribute);
			    }
		    });
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_ATTRIBUTE_LAYOUT, "NAME_WIDTH",
		    new IAttributeHandler() {
			    public void setAttribute(final Object userObject, final String value) {
				    final NodeModel node = (NodeModel) userObject;
				    node.createAttributeTableModel();
				    final AttributeTableLayoutModel layout = node.getAttributes().getLayout();
				    layout.setColumnWidth(0, Integer.parseInt(value));;
			    }
		    });
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_ATTRIBUTE_LAYOUT, "VALUE_WIDTH",
		    new IAttributeHandler() {
			    public void setAttribute(final Object userObject, final String value) {
				    final NodeModel node = (NodeModel) userObject;
				    node.createAttributeTableModel();
				    final AttributeTableLayoutModel layout = node.getAttributes().getLayout();
				    layout.setColumnWidth(1, Integer.parseInt(value));;
			    }
		    });
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_ATTRIBUTE, "NAME",
		    new IAttributeHandler() {
			    public void setAttribute(final Object userObject, final String value) {
				    final AttributeProperties ap = (AttributeProperties) userObject;
				    ap.attributeName = value.toString();
			    }
		    });
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_ATTRIBUTE, "VALUE",
		    new IAttributeHandler() {
			    public void setAttribute(final Object userObject, final String value) {
				    final AttributeProperties ap = (AttributeProperties) userObject;
				    ap.attributeValue = value.toString();
			    }
		    });
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_ATTRIBUTE_REGISTRY, "RESTRICTED",
		    new IAttributeHandler() {
			    public void setAttribute(final Object userObject, final String value) {
				    getMap().getRegistry().getAttributes().setRestricted(true);
			    }
		    });
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_ATTRIBUTE_REGISTRY, "SHOW_ATTRIBUTES",
		    new IAttributeHandler() {
			    public void setAttribute(final Object userObject, final String value) {
				    Controller.getController().getAttributeController().setAttributeViewType(
				        getMap(), value.toString());
			    }
		    });
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_ATTRIBUTE_REGISTRY, "FONT_SIZE",
		    new IAttributeHandler() {
			    public void setAttribute(final Object userObject, final String value) {
				    try {
					    final int size = Integer.parseInt(value.toString());
					    getMap().getRegistry().getAttributes().setFontSize(size);
				    }
				    catch (final NumberFormatException ex) {
				    }
			    }
		    });
	}

	/**
	 */
	public void registerBy(final ReadManager reader, final WriteManager writer) {
		reader.addElementHandler("attribute_registry", this);
		reader.addElementHandler(AttributeBuilder.XML_NODE_ATTRIBUTE, this);
		reader.addElementHandler(AttributeBuilder.XML_NODE_REGISTERED_ATTRIBUTE_NAME, this);
		reader.addElementHandler(AttributeBuilder.XML_NODE_REGISTERED_ATTRIBUTE_VALUE, this);
		writer.addExtensionElementWriter(NodeAttributeTableModel.class, this);
		registerAttributeHandlers(reader);
	}

	public void setAttributes(final String tag, final Object node, final IXMLElement attributes) {
	}

	public void writeContent(final ITreeWriter writer, final Object node, final IExtension extension)
	        throws IOException {
		final NodeAttributeTableModel attributes = (NodeAttributeTableModel) extension;
		attributes.save(writer);
	}
}
