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

import org.freeplane.controller.Controller;
import org.freeplane.extension.IExtension;
import org.freeplane.io.IAttributeHandler;
import org.freeplane.io.INodeCreator;
import org.freeplane.io.INodeWriter;
import org.freeplane.io.ITreeWriter;
import org.freeplane.io.ReadManager;
import org.freeplane.io.WriteManager;
import org.freeplane.io.xml.n3.nanoxml.IXMLElement;
import org.freeplane.map.tree.MapModel;
import org.freeplane.map.tree.MapReader;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.NodeBuilder.NodeObject;

class AttributeBuilder implements INodeCreator, IAttributeHandler,
        INodeWriter<IExtension> {
	static class AttributeLayout {
		int attributeNameWidth = AttributeTableLayoutModel.DEFAULT_COLUMN_WIDTH;
		int attributeValueWidth = AttributeTableLayoutModel.DEFAULT_COLUMN_WIDTH;
	}

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

	public void completeNode(final Object parent, final String tag,
	                         final Object userObject) {
		/* attributes */
		if (tag.equals(AttributeBuilder.XML_NODE_REGISTERED_ATTRIBUTE_NAME)) {
			final RegisteredAttributeProperties rap = (RegisteredAttributeProperties) userObject;
			if (rap.visible) {
				getMap().getRegistry().getAttributes().getElement(
				    rap.attributeName).setVisibility(true);
			}
			if (rap.restricted) {
				getMap().getRegistry().getAttributes().getElement(
				    rap.attributeName).setRestriction(true);
			}
			return;
		}
		if (parent instanceof NodeObject) {
			final NodeModel node = ((NodeObject) parent).node;
			if (tag.equals(AttributeBuilder.XML_NODE_ATTRIBUTE)) {
				final AttributeProperties ap = (AttributeProperties) userObject;
				final Attribute attribute = new Attribute(ap.attributeName,
				    ap.attributeValue);
				node.createAttributeTableModel();
				node.getAttributes().addRowNoUndo(attribute);
				return;
			}
			return;
		}
	}

	public Object createNode(final Object parent, final String tag) {
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

	private MapModel getMap() {
		return mapReader.getCreatedMap();
	}

	public boolean parseAttribute(final Object userObject, final String tag,
	                              final String name, final String value) {
		if (tag.equals(AttributeBuilder.XML_NODE_REGISTERED_ATTRIBUTE_NAME)) {
			final RegisteredAttributeProperties rap = (RegisteredAttributeProperties) userObject;
			if (name.equals("NAME")) {
				rap.attributeName = value;
				getMap().getRegistry().getAttributes().registry(value);
			}
			if (name.equals("VISIBLE")) {
				rap.visible = true;
			}
			if (name.equals("RESTRICTED")) {
				rap.restricted = true;
			}
			return true;
		}
		if (tag.equals(AttributeBuilder.XML_NODE_REGISTERED_ATTRIBUTE_VALUE)
		        && name.equals("VALUE")) {
			final RegisteredAttributeProperties rap = (RegisteredAttributeProperties) userObject;
			final Attribute attribute = new Attribute(rap.attributeName, value);
			final AttributeRegistry r = getMap().getRegistry().getAttributes();
			r.registry(attribute);
			return true;
		}
		if (tag.equals(AttributeBuilder.XML_NODE_ATTRIBUTE_LAYOUT)) {
			final NodeModel node = ((NodeObject) userObject).node;
			node.createAttributeTableModel();
			final AttributeTableLayoutModel layout = node.getAttributes()
			    .getLayout();
			if (name.equals("NAME_WIDTH")) {
				layout.setColumnWidth(0, Integer.parseInt(value));;
			}
			if (name.equals("VALUE_WIDTH")) {
				layout.setColumnWidth(1, Integer.parseInt(value));;
			}
			return true;
		}
		if (tag.equals(AttributeBuilder.XML_NODE_ATTRIBUTE)) {
			final AttributeProperties ap = (AttributeProperties) userObject;
			if (name.equals("NAME")) {
				ap.attributeName = value.toString();
			}
			else if (name.equals("VALUE")) {
				ap.attributeValue = value.toString();
			}
			return true;
		}
		else if (tag.equals(AttributeBuilder.XML_NODE_ATTRIBUTE_LAYOUT)) {
			final AttributeLayout al = (AttributeLayout) userObject;
			if (name.equals("NAME_WIDTH")) {
				al.attributeNameWidth = Integer.parseInt(value.toString());
			}
			else if (name.equals("VALUE_WIDTH")) {
				al.attributeValueWidth = Integer.parseInt(value.toString());
			}
			return true;
		}
		else if (tag.equals(AttributeBuilder.XML_NODE_ATTRIBUTE_REGISTRY)) {
			if (name.equals("RESTRICTED")) {
				getMap().getRegistry().getAttributes().setRestricted(true);
			}
			if (name.equals("SHOW_ATTRIBUTES")) {
				Controller.getController().getAttributeController()
				    .setAttributeViewType(getMap(), value.toString());
			}
			if (name.equals("FONT_SIZE")) {
				try {
					final int size = Integer.parseInt(value.toString());
					getMap().getRegistry().getAttributes().setFontSize(size);
				}
				catch (final NumberFormatException ex) {
				}
			}
			return true;
		}
		return false;
	}

	/**
	 */
	public void registerBy(final ReadManager reader, final WriteManager writer) {
		reader.addNodeCreator("attribute_registry", this);
		reader.addNodeCreator(AttributeBuilder.XML_NODE_ATTRIBUTE, this);
		reader.addNodeCreator(
		    AttributeBuilder.XML_NODE_REGISTERED_ATTRIBUTE_NAME, this);
		reader.addNodeCreator(
		    AttributeBuilder.XML_NODE_REGISTERED_ATTRIBUTE_VALUE, this);
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_ATTRIBUTE_LAYOUT,
		    this);
		writer.addExtensionNodeWriter(NodeAttributeTableModel.class, this);
	}

	public void writeContent(final ITreeWriter writer, final Object node,
	                         final IExtension extension) throws IOException {
		final NodeAttributeTableModel attributes = (NodeAttributeTableModel) extension;
		attributes.save(writer);
	}

	public void setAttributes(String tag, Object node, IXMLElement attributes) {
    }
}
