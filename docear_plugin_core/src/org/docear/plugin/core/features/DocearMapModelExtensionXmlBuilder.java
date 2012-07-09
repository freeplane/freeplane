package org.docear.plugin.core.features;

import java.net.URI;

import org.docear.plugin.core.features.DocearMapModelExtension.DocearMapType;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.IExtensionAttributeWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

public class DocearMapModelExtensionXmlBuilder implements IElementDOMHandler, IExtensionAttributeWriter {
	
	private static final String DOCEAR_MAP_EXTENSION_XML_TAG = "map";
	private static final String DOCEAR_MAP_EXTENSION_DIALECT_KEY = "dialect";
	private static final String DOCEAR_MAP_EXTENSION_VERSION_KEY = "version";
	private static final String DOCEAR_MAP_EXTENSION_TYPE_XML_TAG = "type";
	
	public void registerBy(final ReadManager reader, final WriteManager writer) {
		reader.addElementHandler(DOCEAR_MAP_EXTENSION_XML_TAG, this);
		try {
			registerAttributeHandlers(reader);
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
		writer.addExtensionAttributeWriter(DocearMapModelExtension.class, this);		
	}
	
	private void registerAttributeHandlers(ReadManager reader) {
		
		final IAttributeHandler freeplaneDialectHandler = reader.getAttributeHandlers().get(DOCEAR_MAP_EXTENSION_XML_TAG).get(DOCEAR_MAP_EXTENSION_DIALECT_KEY);
		reader.removeAttributeHandler(DOCEAR_MAP_EXTENSION_XML_TAG, DOCEAR_MAP_EXTENSION_DIALECT_KEY, freeplaneDialectHandler);		
		//DOCEAR - keep dialect handling for backwards compatibility
		reader.addAttributeHandler(DOCEAR_MAP_EXTENSION_XML_TAG, DOCEAR_MAP_EXTENSION_DIALECT_KEY, new IAttributeHandler() {			
			public void setAttribute(Object node, String value) {
				if (!value.startsWith("docear")) {
					freeplaneDialectHandler.setAttribute(node, value);
					return;
				}
				value = value.replace("docear ", "");
				final MapModel mapModel = (MapModel) node;				
				DocearMapModelExtension docearMapModel = mapModel.getExtension(DocearMapModelExtension.class);
				if (docearMapModel == null) {
						docearMapModel = new DocearMapModelExtension(value);
						DocearMapModelController.setModel(mapModel, docearMapModel);
				}				
				docearMapModel.setVersion(value);
				
			}
			
		});	
		
		final IAttributeHandler freeplaneVersionHandler = reader.getAttributeHandlers().get(DOCEAR_MAP_EXTENSION_XML_TAG).get(DOCEAR_MAP_EXTENSION_VERSION_KEY);
		reader.removeAttributeHandler(DOCEAR_MAP_EXTENSION_XML_TAG, DOCEAR_MAP_EXTENSION_VERSION_KEY, freeplaneDialectHandler);		
		reader.addAttributeHandler(DOCEAR_MAP_EXTENSION_XML_TAG, DOCEAR_MAP_EXTENSION_VERSION_KEY, new IAttributeHandler() {			
			public void setAttribute(Object node, String value) {
				if (!value.startsWith("docear")) {
					freeplaneVersionHandler.setAttribute(node, value);
					return;
				}
				value = value.replace("docear ", "");				
				final MapModel mapModel = (MapModel) node;				
				DocearMapModelExtension docearMapModel = mapModel.getExtension(DocearMapModelExtension.class);
				if (docearMapModel == null) {
						docearMapModel = new DocearMapModelExtension(value);
						DocearMapModelController.setModel(mapModel, docearMapModel);
				}
				docearMapModel.setVersion(value);
								
			}
			
		});	
		
		reader.addAttributeHandler(DOCEAR_MAP_EXTENSION_XML_TAG, DOCEAR_MAP_EXTENSION_TYPE_XML_TAG, new IAttributeHandler() {
			
			public void setAttribute(Object node, String value) {
				final MapModel mapModel = (MapModel) node;
				DocearMapModelExtension docearMapModel = mapModel.getExtension(DocearMapModelExtension.class);
				if (docearMapModel == null) {
					docearMapModel = new DocearMapModelExtension();
					DocearMapModelController.setModel(mapModel, docearMapModel);
				}			
				docearMapModel.setType(value);				
			}
			
		});
		
		reader.addAttributeHandler(DOCEAR_MAP_EXTENSION_XML_TAG, DocearMapModelExtension.MAP_ID_ATTRIBUTE, new IAttributeHandler() {
			
			public void setAttribute(Object node, String value) {
				final MapModel mapModel = (MapModel) node;
				
				DocearMapModelExtension docearMapModel = mapModel.getExtension(DocearMapModelExtension.class);
				if (docearMapModel == null) {
					docearMapModel = new DocearMapModelExtension();
					DocearMapModelController.setModel(mapModel, docearMapModel);
				}			
				docearMapModel.setMapId(value);
			}			
		});
		
		reader.addAttributeHandler(DOCEAR_MAP_EXTENSION_XML_TAG, DocearMapModelExtension.MAP_URI_ATTRIBUTE, new IAttributeHandler() {
			
			public void setAttribute(Object node, String value) {
				final MapModel mapModel = (MapModel) node;
				
				DocearMapModelExtension docearMapModel = mapModel.getExtension(DocearMapModelExtension.class);
				if (docearMapModel == null) {
					docearMapModel = new DocearMapModelExtension();
					DocearMapModelController.setModel(mapModel, docearMapModel);
				}			
				docearMapModel.setUri(URI.create(value));
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
		final DocearMapType type = modelExtension.getType();
		if(type != null){
			writer.addAttribute(DOCEAR_MAP_EXTENSION_TYPE_XML_TAG, type.toString());
		}
		final String mapId = modelExtension.getMapId();
		if(mapId != null){
			writer.addAttribute(DocearMapModelExtension.MAP_ID_ATTRIBUTE, mapId);
		}
		final URI mapUri = modelExtension.getUri();
		if(mapUri != null){
			writer.addAttribute(DocearMapModelExtension.MAP_URI_ATTRIBUTE, mapUri.toString());
		}
	}

}
