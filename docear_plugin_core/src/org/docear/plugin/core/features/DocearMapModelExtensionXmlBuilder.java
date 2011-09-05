package org.docear.plugin.core.features;

import java.io.IOException;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.IExtensionElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.features.map.MapModel;
import org.freeplane.n3.nanoxml.XMLElement;

public class DocearMapModelExtensionXmlBuilder implements IElementDOMHandler, IExtensionElementWriter {
	
	private static final String DOCEAR_MAP_EXTENSION_XML_TAG = "docear_map";
	private static final String DOCEAR_MAP_EXTENSION_VERSION_XML_TAG = "version";
	
	public void registerBy(final ReadManager reader, final WriteManager writer) {
		reader.addElementHandler(DOCEAR_MAP_EXTENSION_XML_TAG, this);
		registerAttributeHandlers(reader);
		writer.addExtensionElementWriter(DocearMapModelExtension.class, this);		
	}
	
	private void registerAttributeHandlers(ReadManager reader) {
		reader.addAttributeHandler(DOCEAR_MAP_EXTENSION_XML_TAG, DOCEAR_MAP_EXTENSION_VERSION_XML_TAG, new IAttributeHandler() {
			
			public void setAttribute(Object node, String value) {
				final DocearMapModelExtension mapModelExtension = (DocearMapModelExtension) node;
				mapModelExtension.setVersion(value);			
			}
			
		});	
	}

	public Object createElement(Object parent, String tag, XMLElement attributes) {
		if(tag.equals(DOCEAR_MAP_EXTENSION_XML_TAG)) {
			final DocearMapModelExtension docearMapModel = DocearMapModelController.getModel((MapModel) parent);
			if(docearMapModel != null){
				return docearMapModel;
			}
			else{				
				return new DocearMapModelExtension();				
			}
		}
		return null;
	}	

	public void endElement(Object parent, String tag, Object userObject, XMLElement dom) {
		if (parent instanceof MapModel) {
			final MapModel map = (MapModel) parent;
			if (userObject instanceof DocearMapModelExtension) {
				final DocearMapModelExtension docearMapModel = (DocearMapModelExtension) userObject;
				DocearMapModelController.setModel(map, docearMapModel);
			}
		}
	}
	
	public void writeContent(ITreeWriter writer, Object element, IExtension extension) throws IOException {
		writeContentImpl(writer, null, extension);
	}
	
	public void writeContentImpl(final ITreeWriter writer, final MapModel map, final IExtension extension) throws IOException {
		
		final DocearMapModelExtension modelExtension = extension != null ? (DocearMapModelExtension) extension : DocearMapModelController.getModel(map);
		if (modelExtension == null) {
			return;
		}
		final XMLElement docearMapModelExtension = new XMLElement();
		docearMapModelExtension.setName(DOCEAR_MAP_EXTENSION_XML_TAG);
		
		final String version = modelExtension.getVersion();
		if (version != null && version.length() > 0) {
			docearMapModelExtension.setAttribute(DOCEAR_MAP_EXTENSION_VERSION_XML_TAG, version);
		}		
		
		writer.addElement(modelExtension, docearMapModelExtension);
		
	}

}
