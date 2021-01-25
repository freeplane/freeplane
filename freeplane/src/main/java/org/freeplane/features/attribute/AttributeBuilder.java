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
package org.freeplane.features.attribute;

import java.io.IOException;
import java.util.Vector;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.BackwardCompatibleQuantityWriter;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IAttributeWriter;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.IElementHandler;
import org.freeplane.core.io.IExtensionElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TypeReference;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapReader;
import org.freeplane.features.map.MapWriter;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.TextController;
import org.freeplane.n3.nanoxml.XMLElement;

class AttributeBuilder implements IElementDOMHandler {
	static class AttributeProperties {
		Object parent;
		public AttributeProperties(Object parent) {
	        this.parent = parent;
        }
		String attributeName;
		String attributeValue;
		String attributeObject;
		public Object getValue() {
			Object value;
			if(attributeObject == null)
				value = attributeValue;
            else
                try {
                    value = TypeReference.create(attributeObject);
                }
                catch (Exception e) {
                	LogUtils.warn(e);
                	value = attributeValue;
                }
                return value;
        }
	}

	static class RegisteredAttributeProperties {
		String attributeName;
		boolean manual = false;
		boolean restricted = false;
		boolean visible = false;
	}

	public static final String XML_NODE_ATTRIBUTE = "attribute";
	public static final String XML_NODE_ATTRIBUTE_LAYOUT = "attribute_layout";
	public static final String XML_NODE_ATTRIBUTE_REGISTRY = "attribute_registry";
	public static final String XML_NODE_REGISTERED_ATTRIBUTE_NAME = "attribute_name";
	public static final String XML_NODE_REGISTERED_ATTRIBUTE_VALUE = "attribute_value";
	final private AttributeController attributeController;
// // 	final private Controller controller;
	final private MapReader mapReader;

	public AttributeBuilder(final AttributeController attributeController, final MapReader mapReader) {
		this.attributeController = attributeController;
		this.mapReader = mapReader;
	}

	@Override
	public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
		if (tag.equals(AttributeBuilder.XML_NODE_ATTRIBUTE)
				|| tag.equals(AttributeBuilder.XML_NODE_REGISTERED_ATTRIBUTE_VALUE)) {
			return new AttributeProperties(parent);
		}
		if (tag.equals(AttributeBuilder.XML_NODE_REGISTERED_ATTRIBUTE_NAME)) {
			return new RegisteredAttributeProperties();
		}
		if (tag.equals(AttributeBuilder.XML_NODE_ATTRIBUTE_REGISTRY)) {
			return parent;
		}
		return null;
	}

	@Override
	public void endElement(final Object parent, final String tag, final Object userObject, final XMLElement dom) {
		/* attributes */
		if (tag.equals(AttributeBuilder.XML_NODE_REGISTERED_ATTRIBUTE_NAME)) {
			final RegisteredAttributeProperties rap = (RegisteredAttributeProperties) userObject;
			if (rap.visible) {
				AttributeRegistry.getRegistry(getMap()).getElement(rap.attributeName).setVisibility(true);
			}
			if (rap.restricted) {
				AttributeRegistry.getRegistry(getMap()).getElement(rap.attributeName).setRestriction(true);
			}
			if (rap.manual) {
				AttributeRegistry.getRegistry(getMap()).getElement(rap.attributeName).setManual(true);
			}
			return;
		}
		if (tag.equals(AttributeBuilder.XML_NODE_REGISTERED_ATTRIBUTE_VALUE)) {
			final AttributeProperties ap = (AttributeProperties) userObject;
			final RegisteredAttributeProperties rap = (RegisteredAttributeProperties) ap.parent;
		    final Attribute attribute = new Attribute(rap.attributeName, ap.getValue());
		    final AttributeRegistry r = AttributeRegistry.getRegistry(getMap());
		    r.registry(attribute);
		}
		if (parent instanceof NodeModel) {
			final NodeModel node = (NodeModel) parent;
			if (tag.equals(AttributeBuilder.XML_NODE_ATTRIBUTE)) {
				final AttributeProperties ap = (AttributeProperties) userObject;
				final Attribute attribute = new Attribute(ap.attributeName, ap.getValue());
				attributeController.createAttributeTableModel(node);
				final NodeAttributeTableModel model = NodeAttributeTableModel.getModel(node);
				model.addRowNoUndo(node, attribute);
				return;
			}
			return;
		}
	}

	private MapModel getMap() {
		return mapReader.getCurrentNodeTreeCreator().getCreatedMap();
	}

	private void registerAttributeHandlers(final ReadManager reader) {
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_REGISTERED_ATTRIBUTE_NAME, "NAME",
		    new IAttributeHandler() {
			    @Override
				public void setAttribute(final Object userObject, final String value) {
				    final RegisteredAttributeProperties rap = (RegisteredAttributeProperties) userObject;
				    rap.attributeName = value;
				    AttributeRegistry.getRegistry(getMap()).registry(value);
			    }
		    });
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_REGISTERED_ATTRIBUTE_NAME, "VISIBLE",
		    new IAttributeHandler() {
			    @Override
				public void setAttribute(final Object userObject, final String value) {
				    final RegisteredAttributeProperties rap = (RegisteredAttributeProperties) userObject;
				    rap.visible = true;
			    }
		    });
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_REGISTERED_ATTRIBUTE_NAME, "RESTRICTED",
		    new IAttributeHandler() {
			    @Override
				public void setAttribute(final Object userObject, final String value) {
				    final RegisteredAttributeProperties rap = (RegisteredAttributeProperties) userObject;
				    rap.restricted = true;
			    }
		    });
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_REGISTERED_ATTRIBUTE_NAME, "MANUAL",
		    new IAttributeHandler() {
			    @Override
				public void setAttribute(final Object userObject, final String value) {
				    final RegisteredAttributeProperties rap = (RegisteredAttributeProperties) userObject;
				    rap.manual = true;
			    }
		    });
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_REGISTERED_ATTRIBUTE_VALUE, "VALUE",
		    new IAttributeHandler() {
			    @Override
				public void setAttribute(final Object userObject, final String value) {
				    final AttributeProperties ap = (AttributeProperties) userObject;
				    ap.attributeValue = value;
			    }
		    });
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_REGISTERED_ATTRIBUTE_VALUE, "OBJECT",
		    new IAttributeHandler() {
			    @Override
				public void setAttribute(final Object userObject, final String value) {
				    final AttributeProperties ap = (AttributeProperties) userObject;
				    ap.attributeObject = value;
			    }
		    });
		reader.addElementHandler(XML_NODE_ATTRIBUTE_LAYOUT, new IElementHandler() {
			@Override
			public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
				return parent;
			}
		});
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_ATTRIBUTE_LAYOUT, "NAME_WIDTH", new IAttributeHandler() {
			@Override
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				attributeController.createAttributeTableModel(node);
				final AttributeTableLayoutModel layout = NodeAttributeTableModel.getModel(node).getLayout();
				layout.setColumnWidth(0, LengthUnit.fromStringInPt(value));
			}
		});
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_ATTRIBUTE_LAYOUT, "VALUE_WIDTH", new IAttributeHandler() {
			@Override
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				attributeController.createAttributeTableModel(node);
				final AttributeTableLayoutModel layout = NodeAttributeTableModel.getModel(node).getLayout();
				layout.setColumnWidth(1, LengthUnit.fromStringInPt(value));
			}
		});
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_ATTRIBUTE, "NAME", new IAttributeHandler() {
			@Override
			public void setAttribute(final Object userObject, final String value) {
				final AttributeProperties ap = (AttributeProperties) userObject;
				ap.attributeName = value;
			}
		});
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_ATTRIBUTE, "VALUE", new IAttributeHandler() {
			@Override
			public void setAttribute(final Object userObject, final String value) {
				final AttributeProperties ap = (AttributeProperties) userObject;
				ap.attributeValue = value;
			}
		});
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_ATTRIBUTE, "OBJECT", new IAttributeHandler() {
			@Override
			public void setAttribute(final Object userObject, final String value) {
				final AttributeProperties ap = (AttributeProperties) userObject;
				ap.attributeObject = value;
			}
		});
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_ATTRIBUTE_REGISTRY, "RESTRICTED", new IAttributeHandler() {
			@Override
			public void setAttribute(final Object userObject, final String value) {
				AttributeRegistry.getRegistry(getMap()).setRestricted(true);
			}
		});
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_ATTRIBUTE_REGISTRY, "SHOW_ATTRIBUTES",
		    new IAttributeHandler() {
			    @Override
				public void setAttribute(final Object userObject, final String value) {
					final AttributeRegistry attributes = AttributeRegistry.getRegistry(getMap());
					if(attributes != null)
						attributes.setAttributeViewType(value);
			    }
		    });
		reader.addAttributeHandler(AttributeBuilder.XML_NODE_ATTRIBUTE_REGISTRY, "FONT_SIZE", new IAttributeHandler() {
			@Override
			public void setAttribute(final Object userObject, final String value) {
				final int size = Integer.parseInt(value.toString());
				getMap().addExtension(new FontSizeExtension(size));
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
		writer.addExtensionElementWriter(NodeAttributeTableModel.class, new IExtensionElementWriter() {
			@Override
			public void writeContent(final ITreeWriter writer, final Object node, final IExtension extension)
			        throws IOException {
				final NodeAttributeTableModel attributes = (NodeAttributeTableModel) extension;
				save((NodeModel)node, attributes, writer);
			}
		});
		writer.addExtensionElementWriter(AttributeRegistry.class, new IExtensionElementWriter() {
			@Override
			public void writeContent(final ITreeWriter writer, final Object node, final IExtension extension)
			        throws IOException {
				final AttributeRegistry attributes = (AttributeRegistry) extension;
				attributes.write(writer);
			}
		});
		writer.addAttributeWriter(XML_NODE_ATTRIBUTE_LAYOUT, AttributeWriter.INSTANCE);
		registerAttributeHandlers(reader);
	}

	void save(NodeModel node, NodeAttributeTableModel table, final ITreeWriter writer) throws IOException {
		saveLayout(table.getLayout(), writer);
		if (table.getRowCount() > 0) {
			final Vector<Attribute> attributes = table.getAttributes();
			for (int i = 0; i < attributes.size(); i++) {
				saveAttribute(node, writer, table, attributes.get(i));
			}
		}
	}

	private static final Quantity<LengthUnit> DEFAULT_COLUMN_WIDTH = new Quantity<LengthUnit>(60, LengthUnit.pt);
	static class AttributeWriter implements IAttributeWriter{
		static AttributeWriter INSTANCE = new AttributeWriter();

		@Override
		public void writeAttributes(ITreeWriter writer, Object userObject, String tag) {
			AttributeTableLayoutModel layout = (AttributeTableLayoutModel) userObject;
			final Quantity<LengthUnit> firstColumnWidth = layout.getColumnWidth(0);
			final Quantity<LengthUnit> secondColumnWidth = layout.getColumnWidth(1);
			final boolean firstColumnHasOwnWidth = !DEFAULT_COLUMN_WIDTH.equals(firstColumnWidth);
			final boolean secondColumnHasOwnWidth = !DEFAULT_COLUMN_WIDTH.equals(secondColumnWidth);
			if (firstColumnHasOwnWidth) {
				BackwardCompatibleQuantityWriter.forWriter(writer).writeQuantity("NAME_WIDTH", firstColumnWidth);
			}
			if (secondColumnHasOwnWidth) {
				BackwardCompatibleQuantityWriter.forWriter(writer).writeQuantity("VALUE_WIDTH", secondColumnWidth);
			}
		}

	}
	private void saveLayout(AttributeTableLayoutModel layout, final ITreeWriter writer) throws IOException {
		if (layout != null) {
			final Quantity<LengthUnit> firstColumnWidth = layout.getColumnWidth(0);
			final Quantity<LengthUnit> secondColumnWidth = layout.getColumnWidth(1);
			final boolean firstColumnHasOwnWidth = !DEFAULT_COLUMN_WIDTH.equals(firstColumnWidth);
			final boolean secondColumnHasOwnWidth = !DEFAULT_COLUMN_WIDTH.equals(secondColumnWidth);
			if (firstColumnHasOwnWidth || secondColumnHasOwnWidth ) {
				writer.addElement(layout, AttributeBuilder.XML_NODE_ATTRIBUTE_LAYOUT);
			}
		}
	}

	private void saveAttribute(NodeModel node, final ITreeWriter writer, NodeAttributeTableModel attributes, final Attribute attr) throws IOException {
		final XMLElement attributeElement = new XMLElement();
		attributeElement.setName(AttributeBuilder.XML_NODE_ATTRIBUTE);
		attributeElement.setAttribute("NAME", attr.getName());
		final Object value = attr.getValue();
		final boolean forceFormatting = Boolean.TRUE.equals(writer.getHint(MapWriter.WriterHint.FORCE_FORMATTING));
		if (forceFormatting) {
			attributeElement.setAttribute("VALUE", TextController.getController().getTransformedTextNoThrow(node, attributes, value));
		}
		else{
			attributeElement.setAttribute("VALUE", value.toString());
			if(! (value  instanceof String))
				attributeElement.setAttribute("OBJECT", TypeReference.toSpec(value));
		}
		writer.addElement(attr, attributeElement);
	}

	public void setAttributes(final String tag, final Object node, final XMLElement attributes) {
	}
}
