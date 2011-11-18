package org.docear.plugin.core.features;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map.Entry;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.IExtensionElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

public class DocearNodeModelExtensionXmlBuilder implements IElementDOMHandler, IExtensionElementWriter{	
	
	private static final String DOCEAR_NODE_EXTENSION_KEY_XML_TAG = "key";
	private static final String DOCEAR_NODE_EXTENSION_VALUE_XML_TAG = "value";
	private static final String DOCEAR_NODE_EXTENSION_OBJECT_XML_TAG = "object";
	private static final String DOCEAR_NODE_EXTENSION_XML_TAG = "docear_node_extension";
	private static final String DOCEAR_NODE_EXTENSIONS_XML_TAG = "docear_node_extensions";
	
	public void registerBy(final ReadManager reader, final WriteManager writer) {
		reader.addElementHandler(DOCEAR_NODE_EXTENSION_XML_TAG, this);
		reader.addElementHandler(DOCEAR_NODE_EXTENSIONS_XML_TAG, this);
		writer.addExtensionElementWriter(DocearNodeModelExtension.class, this);	
		registerAttributeHandlers(reader);
	}
	
	private void registerAttributeHandlers(ReadManager reader) {
		reader.addAttributeHandler(DOCEAR_NODE_EXTENSION_XML_TAG, DOCEAR_NODE_EXTENSION_KEY_XML_TAG, new IAttributeHandler() {
			
			public void setAttribute(Object node, String value) {				
				final DocearNodeModelExtension extension = (DocearNodeModelExtension) node;
				extension.setXmlBuilderKey(value);
				extension.putEntry(value, null);			
			}
			
		});
		
		reader.addAttributeHandler(DOCEAR_NODE_EXTENSION_XML_TAG, DOCEAR_NODE_EXTENSION_VALUE_XML_TAG, new IAttributeHandler() {
			
			public void setAttribute(Object node, String value) {				
				final DocearNodeModelExtension extension = (DocearNodeModelExtension) node;
				if(extension.getXmlBuilderKey() != null && extension.getXmlBuilderKey().length() > 0){
					extension.putEntry(extension.getXmlBuilderKey(), value);
				}
				extension.setXmlBuilderKey(null);
			}
			
		});	
		
		reader.addAttributeHandler(DOCEAR_NODE_EXTENSION_XML_TAG, DOCEAR_NODE_EXTENSION_OBJECT_XML_TAG, new IAttributeHandler() {
			
			public void setAttribute(Object node, String value) {
				final DocearNodeModelExtension extension = (DocearNodeModelExtension) node;
				if(extension.getXmlBuilderKey() != null && extension.getXmlBuilderKey().length() > 0){
					extension.putEntry(extension.getXmlBuilderKey(), value);
				}
				extension.setXmlBuilderKey(null);
			}
			
		});	
	}

	public Object createElement(Object parent, String tag, XMLElement attributes) {
		if (tag.equals(DOCEAR_NODE_EXTENSIONS_XML_TAG)) {
			final DocearNodeModelExtension oldDocearNodeModel = DocearNodeModelExtensionController.getModel((NodeModel) parent);
			if(oldDocearNodeModel != null){
				return oldDocearNodeModel;
			}
			else{				
				return new DocearNodeModelExtension();				
			}
		}
		if (tag.equals(DOCEAR_NODE_EXTENSION_XML_TAG)) {
			return parent;			
		}
		return null;
	}
	
	public void endElement(final Object parent, final String tag, final Object userObject, final XMLElement dom) {
		if (parent instanceof NodeModel) {
			final NodeModel node = (NodeModel) parent;
			if (userObject instanceof DocearNodeModelExtension) {
				final DocearNodeModelExtension docearNodeModel = (DocearNodeModelExtension) userObject;
				DocearNodeModelExtensionController.setModel(node, docearNodeModel);
			}
		}
		if (parent instanceof DocearNodeModelExtension) {
			final DocearNodeModelExtension docearNodeModel = (DocearNodeModelExtension) parent;
			if (userObject instanceof AbstractMap.SimpleEntry<?, ?>) {
				@SuppressWarnings("unchecked")
				final AbstractMap.SimpleEntry<String, Object> entry = (AbstractMap.SimpleEntry<String, Object>) userObject;
				docearNodeModel.putEntry(entry);				
			}
		}
	}
	
	public void writeContent(ITreeWriter writer, Object element, IExtension extension) throws IOException {
		writeContentImpl(writer, null, extension);
	}

	public void writeContentImpl(final ITreeWriter writer, final NodeModel node, final IExtension extension) throws IOException {
		
		final DocearNodeModelExtension docearNodeModel = extension != null ? (DocearNodeModelExtension) extension : DocearNodeModelExtensionController.getModel(node);
		if (docearNodeModel == null || docearNodeModel.getMap().size() < 1) {
			return;
		}
		XMLElement docearNodeModelXmlElement = new XMLElement();
		docearNodeModelXmlElement.setName(DOCEAR_NODE_EXTENSIONS_XML_TAG);
		for(Entry<String, Object> entry : docearNodeModel.getAllEntries()){
			XMLElement entryXmlElement = new XMLElement();
			entryXmlElement.setName(DOCEAR_NODE_EXTENSION_XML_TAG);
			if(entry.getKey() != null && entry.getKey().length() > 0){
				entryXmlElement.setAttribute(DOCEAR_NODE_EXTENSION_KEY_XML_TAG, entry.getKey());
				if(entry.getValue() != null){
					if(entry.getValue() instanceof String){
						entryXmlElement.setAttribute(DOCEAR_NODE_EXTENSION_VALUE_XML_TAG, entry.getValue().toString());
					}
					else{
						entryXmlElement.setAttribute(DOCEAR_NODE_EXTENSION_OBJECT_XML_TAG, entry.getValue().toString());
					}
				}
			}
			docearNodeModelXmlElement.addChild(entryXmlElement);
		}			
		writer.addElement(docearNodeModel, docearNodeModelXmlElement);
		
	}

}
