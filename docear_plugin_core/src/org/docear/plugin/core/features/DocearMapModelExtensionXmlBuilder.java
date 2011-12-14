package org.docear.plugin.core.features;

import org.docear.plugin.core.features.DocearMapModelExtension.DocearMapType;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.IExtensionAttributeWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

public class DocearMapModelExtensionXmlBuilder implements IElementDOMHandler, IExtensionAttributeWriter {
	
	private static final String DOCEAR_MAP_EXTENSION_XML_TAG = "map";
	private static final String DOCEAR_MAP_EXTENSION_VERSION_XML_TAG = "dialect";
	private static final String DOCEAR_MAP_EXTENSION_TYPE_XML_TAG = "type";
	
	public void registerBy(final ReadManager reader, final WriteManager writer) {
		reader.addElementHandler(DOCEAR_MAP_EXTENSION_XML_TAG, this);
		registerAttributeHandlers(reader);
		writer.addExtensionAttributeWriter(DocearMapModelExtension.class, this);		
	}
	
	private void registerAttributeHandlers(ReadManager reader) {
		reader.addAttributeHandler(DOCEAR_MAP_EXTENSION_XML_TAG, DOCEAR_MAP_EXTENSION_VERSION_XML_TAG, new IAttributeHandler() {
			
			public void setAttribute(Object node, String value) {
				final MapModel mapModel = (MapModel) node;
				final DocearMapModelExtension docearMapModel = new DocearMapModelExtension();
				value = value.replace("docear ", "");
				docearMapModel.setVersion(value);
				DocearMapModelController.setModel(mapModel, docearMapModel);
			}
			
		});	
		
		reader.addAttributeHandler(DOCEAR_MAP_EXTENSION_XML_TAG, DOCEAR_MAP_EXTENSION_TYPE_XML_TAG, new IAttributeHandler() {
			
			public void setAttribute(Object node, String value) {
				final MapModel mapModel = (MapModel) node;
				final DocearMapModelExtension docearMapModel = new DocearMapModelExtension();				
				docearMapModel.setType(value);
				DocearMapModelController.setModel(mapModel, docearMapModel);
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
	

	public void writeAttributes(ITreeWriter writer, Object userObject,	IExtension extension) {
		final DocearMapModelExtension modelExtension = extension != null ? (DocearMapModelExtension) extension : DocearMapModelController.getModel(((NodeModel) userObject).getMap());
		if (modelExtension == null) {
			return;
		}
		final String version = modelExtension.getVersion();
		if (version != null && version.length() > 0) {
			writer.addAttribute(DOCEAR_MAP_EXTENSION_VERSION_XML_TAG, "docear " + version);			
		}
		final DocearMapType type = modelExtension.getType();
		if(type != null){
			writer.addAttribute(DOCEAR_MAP_EXTENSION_TYPE_XML_TAG, type.toString());
		}
	}

}
