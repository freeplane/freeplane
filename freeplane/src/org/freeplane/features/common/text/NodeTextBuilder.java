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
package org.freeplane.features.common.text;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IAttributeWriter;
import org.freeplane.core.io.IElementContentHandler;
import org.freeplane.core.io.IElementWriter;
import org.freeplane.core.io.IExtensionAttributeWriter;
import org.freeplane.core.io.IExtensionElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.common.map.NodeBuilder;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.styles.StyleFactory;
import org.freeplane.features.common.styles.StyleNamedObject;
import org.freeplane.features.common.styles.StyleString;

import org.freeplane.n3.nanoxml.XMLElement;

public class NodeTextBuilder implements IElementContentHandler, IElementWriter, IAttributeWriter, IExtensionElementWriter, IExtensionAttributeWriter {
	public static final String XML_NODE_TEXT = "TEXT";
	public static final String XML_NODE_LOCALIZED_TEXT = "LOCALIZED_TEXT";
	public static final String XML_NODE_XHTML_CONTENT_TAG = "richcontent";
	public static final String XML_NODE_XHTML_TYPE_NODE = "NODE";
	public static final String XML_NODE_XHTML_TYPE_NOTE = "NOTE";
	public static final String XML_NODE_XHTML_TYPE_DETAILS = "DETAILS";
	public static final String XML_NODE_XHTML_TYPE_TAG = "TYPE";
	private static final String XML_NODE_TEXT_SHORTENED = "TEXT_SHORTENED";

	public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
		if (attributes == null) {
			return null;
		}
		final Object typeAttribute = attributes.getAttribute(NodeTextBuilder.XML_NODE_XHTML_TYPE_TAG, null);
		if (NodeTextBuilder.XML_NODE_XHTML_TYPE_NODE.equals(typeAttribute)
				 || NodeTextBuilder.XML_NODE_XHTML_TYPE_DETAILS.equals(typeAttribute)) {
			return parent;
		}
		return null;
	}

	public void endElement(final Object parent, final String tag, final Object obj, final XMLElement attributes,
	                       final String content) {
		assert tag.equals("richcontent");
		final String xmlText = content;
		final Object typeAttribute = attributes.getAttribute(NodeTextBuilder.XML_NODE_XHTML_TYPE_TAG, null);
		final NodeModel nodeModel = (NodeModel) obj;
		if (NodeTextBuilder.XML_NODE_XHTML_TYPE_NODE.equals(typeAttribute)) {
			nodeModel.setXmlText(xmlText);
		}
		else if (NodeTextBuilder.XML_NODE_XHTML_TYPE_DETAILS.equals(typeAttribute)) {
			final DetailTextModel note = new DetailTextModel("true".equals(attributes.getAttribute("HIDDEN", "false")));
			note.setXml(xmlText);
			nodeModel.addExtension((IExtension) note);
			TextController.getController().setDetailsTooltip(nodeModel);
		}
	}

	static private class TypeReference{
		Constructor<?> constructor;

		public TypeReference(String typeReference) {
            Constructor<?> constructor;
            try {
	            constructor = getClass().getClassLoader().loadClass(typeReference).getConstructor(String.class);
	            this.constructor = constructor;
            }
            catch (Exception e) {
            	this.constructor = null;
	            e.printStackTrace();
            }
        }
		Object create(String spec){
			try {
				return constructor.newInstance(spec);
			}
			catch (Exception e) {
				LogUtils.warn(e);
				return spec;
			}
		}
	}
	private void registerAttributeHandlers(final ReadManager reader) {
		reader.addAttributeHandler(NodeBuilder.XML_NODE, NodeTextBuilder.XML_NODE_TEXT, new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = ((NodeModel) userObject);
				final Object nodeContent = node.getUserObject();
				if(nodeContent instanceof TypeReference){
					final Object newInstance = ((TypeReference) nodeContent).create(nodeContent.toString());
					node.setUserObject(newInstance);
					return;
				}
				node.setText(value);
			}
		});
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "TYPE", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = ((NodeModel) userObject);
				final Object nodeContent = node.getUserObject();
				final TypeReference typeReference = new TypeReference(value);
				if(nodeContent == null){
					node.setUserObject(typeReference);
					return;
				}
				if(nodeContent instanceof String){
					final Object newInstance = typeReference.create(nodeContent.toString());
					node.setUserObject(newInstance);
				}
			}
		});
		reader.addAttributeHandler(NodeBuilder.XML_NODE, NodeTextBuilder.XML_NODE_TEXT_SHORTENED, new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = ((NodeModel) userObject);
				try {
					if(Boolean.valueOf(value)){
						node.addExtension(new ShortenedTextModel());
						TextController.getController().setNodeTextTooltip(node);
					}
				}
				catch (Exception e) {
					LogUtils.warn(e);
				}
			}
		});
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, NodeTextBuilder.XML_NODE_TEXT, new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = ((NodeModel) userObject);
				node.setUserObject(StyleFactory.create(value));
			}
		});
		reader.addAttributeHandler(NodeBuilder.XML_NODE, NodeTextBuilder.XML_NODE_LOCALIZED_TEXT, new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = ((NodeModel) userObject);
				node.setUserObject(StyleFactory.create(NamedObject.format(value)));
			}
		});
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, NodeTextBuilder.XML_NODE_LOCALIZED_TEXT, new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = ((NodeModel) userObject);
				node.setUserObject(StyleFactory.create(NamedObject.format(value)));
			}
		});
	}

	/**
	 * @param writeManager 
	 */
	public void registerBy(final ReadManager reader, final WriteManager writeManager) {
		registerAttributeHandlers(reader);
		reader.addElementHandler("richcontent", this);
		writeManager.addElementWriter(NodeBuilder.XML_NODE, this);
		writeManager.addElementWriter(NodeBuilder.XML_STYLENODE, this);
		writeManager.addAttributeWriter(NodeBuilder.XML_NODE, this);
		writeManager.addAttributeWriter(NodeBuilder.XML_STYLENODE, this);
	}

	public void writeAttributes(final ITreeWriter writer, final Object userObject, final String tag) {
		final Object data = ((NodeModel) userObject).getUserObject();
		final Class<? extends Object> dataClass = data.getClass();
		if (dataClass.equals(StyleNamedObject.class)) {
			writer.addAttribute(NodeTextBuilder.XML_NODE_LOCALIZED_TEXT, ((StyleNamedObject) data).getObject().toString());
			return;
		}
		if (dataClass.equals(NamedObject.class)) {
			writer.addAttribute(NodeTextBuilder.XML_NODE_LOCALIZED_TEXT, ((NamedObject) data).getObject().toString());
			return;
		}
		if (!(dataClass.equals(StyleString.class) || dataClass.equals(String.class))) {
			return;
		}
		final String text =  data.toString();
		if (!HtmlUtils.isHtmlNode(text)) {
			writer.addAttribute(NodeTextBuilder.XML_NODE_TEXT, text.replace('\0', ' '));
		}
		if(! (data instanceof String)){
			writer.addAttribute("TYPE", data.getClass().getName());
		}
	}

	public void writeContent(final ITreeWriter writer, final Object element, final String tag) throws IOException {
		final NodeModel node = (NodeModel) element;
		if (HtmlUtils.isHtmlNode(node.getText())) {
			final XMLElement htmlElement = new XMLElement();
			htmlElement.setName(NodeTextBuilder.XML_NODE_XHTML_CONTENT_TAG);
			htmlElement.setAttribute(NodeTextBuilder.XML_NODE_XHTML_TYPE_TAG, NodeTextBuilder.XML_NODE_XHTML_TYPE_NODE);
			final String xmlText = node.getXmlText();
			final String content = xmlText.replace('\0', ' ');
			writer.addElement(content, htmlElement);
		}
	}
	/*
	 * (non-Javadoc)
	 * @see freeplane.io.INodeWriter#saveContent(freeplane.io.ITreeWriter,
	 * java.lang.Object, java.lang.String)
	 */
	public void writeContent(final ITreeWriter writer, final Object element, final IExtension note) throws IOException {
		DetailTextModel model = (DetailTextModel) note;
		if (model.getXml() != null) {
			final XMLElement htmlElement = new XMLElement();
			htmlElement.setName(NodeTextBuilder.XML_NODE_XHTML_CONTENT_TAG);
			htmlElement.setAttribute(NodeTextBuilder.XML_NODE_XHTML_TYPE_TAG, NodeTextBuilder.XML_NODE_XHTML_TYPE_DETAILS);
			if(model.isHidden()){
				htmlElement.setAttribute("HIDDEN", "true");
			}
			final String content = model.getXml().replace('\0', ' ');
			writer.addElement(content, htmlElement);
		}
		return;
	}

	public void writeAttributes(ITreeWriter writer, Object userObject, IExtension extension) {
		writer.addAttribute(XML_NODE_TEXT_SHORTENED, Boolean.TRUE.toString());
    }
}
