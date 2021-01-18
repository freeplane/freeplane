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
package org.freeplane.features.text;

import java.io.IOException;

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
import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.core.util.TypeReference;
import org.freeplane.features.format.IFormattedObject;
import org.freeplane.features.map.MapWriter;
import org.freeplane.features.map.NodeBuilder;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeWriter;
import org.freeplane.features.styles.StyleFactory;
import org.freeplane.features.styles.StyleTranslatedObject;
import org.freeplane.features.styles.StyleString;
import org.freeplane.n3.nanoxml.XMLElement;

public class NodeTextBuilder implements IElementContentHandler, IElementWriter, IAttributeWriter, IExtensionElementWriter, IExtensionAttributeWriter {
	public static final String XML_NODE_TEXT = "TEXT";
	public static final String XML_NODE_LOCALIZED_TEXT = "LOCALIZED_TEXT";
	public static final String XML_NODE_RICHCONTENT_TAG = "richcontent";
	public static final String XML_RICHCONTENT_TYPE_NODE = "NODE";
	public static final String XML_RICHCONTENT_TYPE_NOTE = "NOTE";
	public static final String XML_RICHCONTENT_TYPE_DETAILS = "DETAILS";
	public static final String XML_RICHCONTENT_TYPE_ATTRIBUTE = "TYPE";
    public static final String XML_RICHCONTENT_CONTENT_TYPE_ATTRIBUTE = "CONTENT-TYPE";
    public static final String XML_NODE_OBJECT = "OBJECT";
	private static final String XML_NODE_TEXT_SHORTENED = "TEXT_SHORTENED";
	
	public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
		if (attributes == null) {
			return null;
		}
		final Object typeAttribute = attributes.getAttribute(NodeTextBuilder.XML_RICHCONTENT_TYPE_ATTRIBUTE, null);
		if (NodeTextBuilder.XML_RICHCONTENT_TYPE_NODE.equals(typeAttribute)
				 || NodeTextBuilder.XML_RICHCONTENT_TYPE_DETAILS.equals(typeAttribute)) {
			return parent;
		}
		return null;
	}


    @Override
    public boolean containsXml(XMLElement element) {
        return ContentSyntax.XML.matches(element.getAttribute(NodeTextBuilder.XML_RICHCONTENT_CONTENT_TYPE_ATTRIBUTE, ContentSyntax.XML.prefix));
    }

	public void endElement(final Object parent, final String tag, final Object obj, final XMLElement element,
	                       final String content) {
		assert tag.equals("richcontent");
		final String text;
		final Object localizedHtml = element.getAttribute("LOCALIZED_HTML", null);
		if(localizedHtml != null)
			text = TextUtils.getRawText((String)localizedHtml);
		else if(content != null)
			text = content.trim();
		else
		    text = null;
		final String type = element.getAttribute(NodeTextBuilder.XML_RICHCONTENT_TYPE_ATTRIBUTE, null);
		final NodeModel nodeModel = (NodeModel) obj;
		if (NodeTextBuilder.XML_RICHCONTENT_TYPE_NODE.equals(type)) {
			nodeModel.setXmlText(text);
		}
		else if (NodeTextBuilder.XML_RICHCONTENT_TYPE_DETAILS.equals(type)) {
			final boolean hidden = "true".equals(element.getAttribute("HIDDEN", "false"));
			final DetailModel details = new DetailModel(hidden);
			if(containsXml(element))
			    details.setXml(text);
			else
			    details.setText(text);
            final String contentType = element.getAttribute(
                    NodeTextBuilder.XML_RICHCONTENT_CONTENT_TYPE_ATTRIBUTE, 
                    ContentSyntax.XML.prefix);
            details.setContentType(ContentSyntax.specificType(contentType));
			nodeModel.addExtension(details);
			if(localizedHtml != null) {
				details.setLocalizedHtmlPropertyName((String)localizedHtml);
			}
		}
	}

	private void registerAttributeHandlers(final ReadManager reader) {
		reader.addAttributeHandler(NodeBuilder.XML_NODE, NodeTextBuilder.XML_NODE_TEXT, new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = ((NodeModel) userObject);
				final Object nodeContent = node.getUserObject();
				if(nodeContent == null || nodeContent.equals("")){
					node.setText(value);
				}
			}
		});
		reader.addAttributeHandler(NodeBuilder.XML_NODE, NodeTextBuilder.XML_NODE_OBJECT, new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = ((NodeModel) userObject);
				final Object newInstance = TypeReference.create(value);
				// work around for old maps :
				// actually we do not need IFormattedObject as user objects
				// because formatting is saved as an extra attribute
				if(newInstance instanceof IFormattedObject)
					node.setUserObject(((IFormattedObject) newInstance).getObject());
				else
					node.setUserObject(newInstance);
			}
		});
		IAttributeHandler textShortenedHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = ((NodeModel) userObject);
				try {
					if(Boolean.valueOf(value)){
						node.addExtension(new ShortenedTextModel());
					}
				}
				catch (Exception e) {
					LogUtils.warn(e);
				}
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, NodeTextBuilder.XML_NODE_TEXT_SHORTENED, textShortenedHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, NodeTextBuilder.XML_NODE_TEXT_SHORTENED, textShortenedHandler);
		
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, NodeTextBuilder.XML_NODE_TEXT, new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = ((NodeModel) userObject);
				node.setUserObject(StyleFactory.create(value));
			}
		});
		reader.addAttributeHandler(NodeBuilder.XML_NODE, NodeTextBuilder.XML_NODE_LOCALIZED_TEXT, new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = ((NodeModel) userObject);
				node.setUserObject(StyleFactory.create(TranslatedObject.format(value)));
			}
		});
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, NodeTextBuilder.XML_NODE_LOCALIZED_TEXT, new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = ((NodeModel) userObject);
				node.setUserObject(StyleFactory.create(TranslatedObject.format(value)));
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

	private static class TransformedXMLExtension implements IExtension{
		final String xml;

		public TransformedXMLExtension(String html) {
	        super();
	        this.xml = HtmlUtils.toXhtml(html);
        }
	}
	public void writeAttributes(final ITreeWriter writer, final Object userObject, final String tag) {
		if(! NodeWriter.shouldWriteSharedContent(writer))
			return;
		final NodeModel node = (NodeModel) userObject;
		final Object data = node.getUserObject();
		if(data == null)
			return;
		final Class<? extends Object> dataClass = data.getClass();
		if (dataClass.equals(StyleTranslatedObject.class)) {
			writer.addAttribute(NodeTextBuilder.XML_NODE_LOCALIZED_TEXT, ((StyleTranslatedObject) data).getObject().toString());
			return;
		}
		if (dataClass.equals(TranslatedObject.class)) {
			writer.addAttribute(NodeTextBuilder.XML_NODE_LOCALIZED_TEXT, ((TranslatedObject) data).getObject().toString());
			return;
		}
		final boolean forceFormatting = Boolean.TRUE.equals(writer.getHint(MapWriter.WriterHint.FORCE_FORMATTING));
		if (forceFormatting) {
			final String text = TextController.getController().getTransformedTextNoThrow(data, node, node);
			if (!HtmlUtils.isHtml(text)) {
				writer.addAttribute(NodeTextBuilder.XML_NODE_TEXT, text.replace('\0', ' '));
			}
			else{
				node.addExtension(new TransformedXMLExtension(text));
			}
		}
		else{
			final String text =  data.toString();
			if (node.getXmlText() == null) {
				writer.addAttribute(NodeTextBuilder.XML_NODE_TEXT, text.replace('\0', ' '));
			}
			if(! (data instanceof String || data instanceof StyleString)){
				writer.addAttribute(XML_NODE_OBJECT, TypeReference.toSpec(data));
			}
		}
	}

	public void writeContent(final ITreeWriter writer, final Object element, final String tag) throws IOException {
		if(! NodeWriter.shouldWriteSharedContent(writer))
			return;
		final NodeModel node = (NodeModel) element;
		final TransformedXMLExtension transformedXML = node.getExtension(TransformedXMLExtension.class);
		if (transformedXML != null || node.getXmlText() != null) {
			final XMLElement htmlElement = new XMLElement();
			htmlElement.setName(NodeTextBuilder.XML_NODE_RICHCONTENT_TAG);
			htmlElement.setAttribute(NodeTextBuilder.XML_RICHCONTENT_TYPE_ATTRIBUTE, NodeTextBuilder.XML_RICHCONTENT_TYPE_NODE);
			final String xmlText;
			if (transformedXML != null){
				xmlText = transformedXML.xml;
				node.removeExtension(transformedXML);
			}
			else
				xmlText = node.getXmlText();
			final String content = xmlText.replace('\0', ' ');
			writer.addElement('\n' + content + '\n', htmlElement);
		}
	}
	/*
	 * (non-Javadoc)
	 * @see freeplane.io.INodeWriter#saveContent(freeplane.io.ITreeWriter,
	 * java.lang.Object, java.lang.String)
	 */
	public void writeContent(final ITreeWriter writer, final Object object, final IExtension note) throws IOException {
		DetailModel model = (DetailModel) note;
		final XMLElement element = new XMLElement();
		element.setName(NodeTextBuilder.XML_NODE_RICHCONTENT_TAG);
		boolean containsXml = model.getXml() != null;
        String contentType = model.getContentType();
        ContentSyntax contentSyntax = containsXml ? ContentSyntax.XML : ContentSyntax.PLAIN;
        element.setAttribute(NodeTextBuilder.XML_RICHCONTENT_CONTENT_TYPE_ATTRIBUTE, contentSyntax.with(contentType));
		element.setAttribute(NodeTextBuilder.XML_RICHCONTENT_TYPE_ATTRIBUTE, NodeTextBuilder.XML_RICHCONTENT_TYPE_DETAILS);
		if(model.isHidden()){
		    element.setAttribute("HIDDEN", "true");
		}
		if(model.getLocalizedHtmlPropertyName() != null){
		    element.setAttribute("LOCALIZED_HTML", model.getLocalizedHtmlPropertyName());
		    writer.addElement(null, element);
		} else {
            if (containsXml) {
            		final String content = model.getXml().replace('\0', ' ');
            		writer.addElement('\n' + content + '\n', element);
            }
            else {
                String text = model.getText();
                if(text != null) {
                    element.setContent(text);
                }
                writer.addElement(null, element);
            }
        }
		return;
	}

	public void writeAttributes(ITreeWriter writer, Object userObject, IExtension extension) {
		writer.addAttribute(XML_NODE_TEXT_SHORTENED, Boolean.TRUE.toString());
    }
}
