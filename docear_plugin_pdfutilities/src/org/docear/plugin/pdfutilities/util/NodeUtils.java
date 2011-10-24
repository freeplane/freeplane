package org.docear.plugin.pdfutilities.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.docear.plugin.core.CoreConfiguration;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.util.Tools;
import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.pdfutilities.features.AnnotationController;
import org.docear.plugin.pdfutilities.features.AnnotationID;
import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.docear.plugin.pdfutilities.features.AnnotationNodeModel;
import org.docear.plugin.pdfutilities.features.IAnnotation;
import org.docear.plugin.pdfutilities.features.IAnnotation.AnnotationType;
import org.docear.plugin.pdfutilities.pdf.PdfFileFilter;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.attribute.AttributeView;


public class NodeUtils {
	
	private final MMapController currentMapController;
	
	public NodeUtils(){
		this.currentMapController = (MMapController) Controller.getCurrentModeController().getMapController();
		
	}
	
	public static boolean isMapCurrentlyOpened(MapModel map){
		Map<String, MapModel> maps = Controller.getCurrentController().getMapViewManager().getMaps();
		for(Entry<String, MapModel> entry : maps.entrySet()){
			if(entry.getValue().getFile().equals(map.getFile())){
				return true;
			}
		}
		return false;
	}
	
	public static boolean saveMap(MapModel map){		
		try {
			((MFileManager) UrlManager.getController()).writeToFile(map, map.getFile());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;			
		}
		return true;
	}
	
	public Map<AnnotationID, Collection<AnnotationNodeModel>> getOldAnnotationsFromMaps(Collection<URI> mindmaps){
		Map<AnnotationID, Collection<AnnotationNodeModel>> result = new HashMap<AnnotationID, Collection<AnnotationNodeModel>>();
		for(MapModel map : this.getMapsFromUris(mindmaps)){
			
			Map<AnnotationID, Collection<AnnotationNodeModel>> temp = this.getOldAnnotationsFrom(map.getRootNode());
			for(AnnotationID id : temp.keySet()){
				if(!result.containsKey(id)){
					result.put(id, new ArrayList<AnnotationNodeModel>());				
				}
				result.get(id).addAll(temp.get(id));
			}
		}
		return result;
	}
	
	public Map<AnnotationID, Collection<AnnotationNodeModel>> getOldAnnotationsFromMap(URI mindmap){
		MapModel map = this.getMapFromUri(mindmap);
		if(map != null){
			return this.getOldAnnotationsFrom(map.getRootNode());
		}
		return new HashMap<AnnotationID, Collection<AnnotationNodeModel>>();
	}
	
	public List<MapModel> getMapsFromUris(Collection<URI> mindmaps){
		List<MapModel> maps = new ArrayList<MapModel>();
		for(URI uri : mindmaps){
			MapModel map = getMapFromUri(uri);
			if(map != null){
				maps.add(map);
			}
		}
		return maps;
	}
	
	
	public MapModel getMapFromUri(URI uri) {
		Map<String, MapModel> maps = Controller.getCurrentController().getMapViewManager().getMaps();
		for(Entry<String, MapModel> entry : maps.entrySet()){
			if(entry.getValue().getFile().toURI().equals(uri)){
				return entry.getValue();
			}
		}
		try {
			final UrlManager urlManager = (UrlManager) Controller.getCurrentModeController().getExtension(UrlManager.class);			
			MapModel map = new MapModel(null);			
			AttributeRegistry.createRegistry(map);
			URL url = Tools.getFilefromUri(uri).toURL();
			urlManager.loadImpl(url, map);
			return map;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Map<AnnotationID, Collection<AnnotationNodeModel>> getOldAnnotationsFromCurrentMap(){
		return getOldAnnotationsFrom(this.currentMapController.getRootNode());
	}
	
	private Map<AnnotationID, Collection<AnnotationNodeModel>> getOldAnnotationsFrom(NodeModel parent){
		Map<AnnotationID, Collection<AnnotationNodeModel>> result = new HashMap<AnnotationID, Collection<AnnotationNodeModel>>();
		try {
			Thread.sleep(1L);
			if(Thread.currentThread().isInterrupted()) return result;				
		} catch (InterruptedException e) {			
		}
		if(isPdfLinkedNode(parent)){
			URI uri = Tools.getAbsoluteUri(parent);
			AnnotationNodeModel oldAnnotation = AnnotationController.getAnnotationNodeModel(parent);
			if(uri != null && oldAnnotation != null){				
				result.put(oldAnnotation.getAnnotationID(), new ArrayList<AnnotationNodeModel>());				
				result.get(oldAnnotation.getAnnotationID()).add(oldAnnotation);
			}		 
		}
		
		for(NodeModel child : parent.getChildren()){
			Map<AnnotationID, Collection<AnnotationNodeModel>> children = getOldAnnotationsFrom(child);
			for(AnnotationID id : children.keySet()){
				if(!result.containsKey(id)){
					result.put(id, new ArrayList<AnnotationNodeModel>());				
				}
				result.get(id).addAll(children.get(id));
			}
		}
		
		return result;
	}
	
	public NodeModel insertChildNodesFromPdf(URI pdfFile, List<AnnotationModel> annotations, boolean isLeft, NodeModel target){
		NodeModel node = this.insertChildNodeFrom(pdfFile, isLeft, target, AnnotationType.PDF_FILE);
		this.insertChildNodesFrom(annotations, isLeft, node);
		return node;
	}
	
	public List<NodeModel> insertChildNodesFrom(List<AnnotationModel> annotations, boolean isLeft, NodeModel target){
		List<NodeModel> nodes = new ArrayList<NodeModel>();
		
		for(AnnotationModel annotation : annotations){
			NodeModel node = this.insertChildNodeFrom(annotation.getUri(), annotation, isLeft, target);
			this.insertChildNodesFrom(annotation.getChildren(), isLeft, node);
			nodes.add(node);
		}
		
		return nodes;
	}
	
	public NodeModel insertChildNodeFrom(URI file, boolean isLeft, NodeModel target, AnnotationType type){
		final NodeModel node = this.currentMapController.newNode(Tools.getFilefromUri(file).getName(), target.getMap());
		this.setLinkFrom(file, node);
		
		if(type != null){
			IAnnotation model = new AnnotationModel();
			model.setAnnotationType(type);
			AnnotationController.setModel(node, model);
		}
		
		return this.insertChildNodeFrom(node, isLeft, target);
	}
	
	public NodeModel insertChildNodeFrom(URI file, IAnnotation annotation, boolean isLeft, NodeModel target){		
		final NodeModel node = this.currentMapController.newNode(annotation.getTitle(), target.getMap());
		this.setLinkFrom(file, node);
		AnnotationController.setModel(node, annotation);
		
		return insertChildNodeFrom(node, isLeft, target);
	}
	
	public NodeModel setLinkFrom(URI file, NodeModel node){		
		((MLinkController) LinkController.getController()).setLinkTypeDependantLink(node, file);
		
		return node;
	}
	
	public NodeModel insertChildNodeFrom(NodeModel node, boolean isLeft, NodeModel target){
		
		this.currentMapController.insertNode(node, target, false, isLeft, isLeft);
		
		return node;
	}
	
	public static boolean isPdfLinkedNode(NodeModel node){
		URI link = Tools.getAbsoluteUri(node);		
        return new PdfFileFilter().accept(link);
    }
	
	public List<NodeModel> insertNewChildNodesFrom(URI pdfFile, Collection<AnnotationModel> annotations, boolean isLeft, NodeModel target){
		AnnotationModel root = new AnnotationModel(new AnnotationID(Tools.getAbsoluteUri(pdfFile), 0), AnnotationType.PDF_FILE);
		root.setTitle(Tools.getFilefromUri(Tools.getAbsoluteUri(pdfFile)).getName());
		root.getChildren().addAll(annotations);
		Collection<AnnotationModel> newList = new ArrayList<AnnotationModel>();
		newList.add(root);
		return insertNewChildNodesFrom(newList, isLeft, target, target);
	}

	public List<NodeModel> insertNewChildNodesFrom(Collection<AnnotationModel> annotations, boolean isLeft, NodeModel target, NodeModel rootTarget) {
		List<NodeModel> nodes = new ArrayList<NodeModel>();
		
		for(AnnotationModel annotation : annotations){			
			if(annotation.isNew() || annotation.hasNewChildren()){
				NodeModel equalChild = targetHasEqualChild(rootTarget, annotation);
				
				if(equalChild == null){
					NodeModel node = this.insertChildNodeFrom(annotation.getUri(), annotation, isLeft, target);
					this.insertNewChildNodesFrom(annotation.getChildren(), isLeft, node, rootTarget);
					nodes.add(node);
				}
				else{
					this.insertNewChildNodesFrom(annotation.getChildren(), isLeft, equalChild, rootTarget);
					nodes.add(equalChild);
				}
				
			}		
		}
		
		return nodes;
	}
	
	public NodeModel targetHasEqualChild(NodeModel target, IAnnotation annotation){
		if(annotation == null)	return null;
		
		for(NodeModel child : target.getChildren()){
			IAnnotation oldAnnotation = AnnotationController.getAnnotationNodeModel(child);
			NodeModel equalChild = this.targetHasEqualChild(child, annotation);
			if(equalChild != null){
				return equalChild;
			}
			if(oldAnnotation == null || oldAnnotation.getAnnotationType() != annotation.getAnnotationType()){
				continue;
			}
			if(annotation.getAnnotationType().equals(AnnotationType.PDF_FILE)){
				if(annotation.getUri().equals(Tools.getAbsoluteUri(child))){
					return child;
				}
			}			
			if(oldAnnotation != null && oldAnnotation.getAnnotationID().equals(annotation.getAnnotationID())){
				return child;
			}
		}
		return null;
	}

	public static boolean isMonitoringNode(NodeModel node) {
		NodeAttributeTableModel attributeModel = (NodeAttributeTableModel) node.getExtension(NodeAttributeTableModel.class);
		return (attributeModel != null && attributeModel.getAttributeKeyList().contains(TextUtils.getText(PdfUtilitiesController.MON_INCOMING_FOLDER)));
	}

	public static URI getPdfDirFromMonitoringNode(NodeModel node) {
		if(!NodeUtils.isMonitoringNode(node)) return null;
		NodeAttributeTableModel attributeModel = (NodeAttributeTableModel) node.getExtension(NodeAttributeTableModel.class);
		if(attributeModel == null || !attributeModel.getAttributeKeyList().contains(TextUtils.getText(PdfUtilitiesController.MON_INCOMING_FOLDER))){
			return null;
		}
		
		return (URI)attributeModel.getValue(attributeModel.getAttributePosition(TextUtils.getText(PdfUtilitiesController.MON_INCOMING_FOLDER)));
	}
	
	public static List<URI> getMindmapDirFromMonitoringNode(NodeModel node) {
		if(!NodeUtils.isMonitoringNode(node)) return null;
		NodeAttributeTableModel attributeModel = (NodeAttributeTableModel) node.getExtension(NodeAttributeTableModel.class);
		if(attributeModel == null || !attributeModel.getAttributeKeyList().contains(TextUtils.getText(PdfUtilitiesController.MON_MINDMAP_FOLDER))){
			return null;
		}
		
		Object value = attributeModel.getValue(attributeModel.getAttributePosition(TextUtils.getText(PdfUtilitiesController.MON_MINDMAP_FOLDER)));
		
		if(value.toString().equals(CoreConfiguration.LIBRARY_PATH)){
			return DocearController.getController().getLibrary().getMindmaps();
		}
		else{
			List<URI> uriList = new ArrayList<URI>();
			uriList.add((URI)value);
			return uriList;
		}		
	}

	public static void removeMonitoringEntries(NodeModel selected) {
		NodeAttributeTableModel attributeModel = (NodeAttributeTableModel) selected.getExtension(NodeAttributeTableModel.class);
		if(attributeModel == null) return;
		
		if(attributeModel.getAttributeKeyList().contains(TextUtils.getText(PdfUtilitiesController.MON_INCOMING_FOLDER))){
			AttributeController.getController(Controller.getCurrentModeController()).performRemoveRow(attributeModel, attributeModel.getAttributePosition(TextUtils.getText(PdfUtilitiesController.MON_INCOMING_FOLDER)));			
		}
		
		if(attributeModel.getAttributeKeyList().contains(TextUtils.getText(PdfUtilitiesController.MON_MINDMAP_FOLDER))){
			AttributeController.getController(Controller.getCurrentModeController()).performRemoveRow(attributeModel, attributeModel.getAttributePosition(TextUtils.getText(PdfUtilitiesController.MON_MINDMAP_FOLDER)));			
		}
		
		if(attributeModel.getAttributeKeyList().contains(TextUtils.getText(PdfUtilitiesController.MON_AUTO))){
			AttributeController.getController(Controller.getCurrentModeController()).performRemoveRow(attributeModel, attributeModel.getAttributePosition(TextUtils.getText(PdfUtilitiesController.MON_AUTO)));			
		}
		
		if(attributeModel.getAttributeKeyList().contains(TextUtils.getText(PdfUtilitiesController.MON_SUBDIRS))){
			AttributeController.getController(Controller.getCurrentModeController()).performRemoveRow(attributeModel, attributeModel.getAttributePosition(TextUtils.getText(PdfUtilitiesController.MON_SUBDIRS)));			
		}
	}
	
	public static boolean addMonitoringDir(NodeModel target, URI monitoringDir){
		if(target == null || monitoringDir == null) return false;
		
		NodeAttributeTableModel attributes = AttributeController.getController().createAttributeTableModel(target);
		if(attributes != null){
			AttributeController.getController().performInsertRow(attributes, attributes.getRowCount(), TextUtils.getText(PdfUtilitiesController.MON_INCOMING_FOLDER), monitoringDir); //$NON-NLS-1$
			AttributeView attributeView = (((MapView) Controller.getCurrentController().getViewController().getMapView()).getSelected()).getAttributeView();
    		attributeView.setOptimalColumnWidths();
			return true;
		}
		return false;
	}
	
	public static boolean addMindmapDir(NodeModel target, URI mindmapDir){
		if(target == null || mindmapDir == null) return false;
		
		NodeAttributeTableModel attributes = AttributeController.getController().createAttributeTableModel(target);
		if(attributes != null){
			AttributeController.getController().performInsertRow(attributes, attributes.getRowCount(), TextUtils.getText(PdfUtilitiesController.MON_MINDMAP_FOLDER), mindmapDir); //$NON-NLS-1$
			AttributeView attributeView = (((MapView) Controller.getCurrentController().getViewController().getMapView()).getSelected()).getAttributeView();
    		attributeView.setOptimalColumnWidths();
			return true;
		}
		return false;
	}
	
	public static boolean addMindmapDir(NodeModel target, String value){
		if(target == null || value == null) return false;
		
		NodeAttributeTableModel attributes = AttributeController.getController().createAttributeTableModel(target);
		if(attributes != null){
			AttributeController.getController().performInsertRow(attributes, attributes.getRowCount(), TextUtils.getText(PdfUtilitiesController.MON_MINDMAP_FOLDER), value); //$NON-NLS-1$
			AttributeView attributeView = (((MapView) Controller.getCurrentController().getViewController().getMapView()).getSelected()).getAttributeView();
    		attributeView.setOptimalColumnWidths();
			return true;
		}
		return false;
	}
	
	public static boolean setAttributeValue(NodeModel target, String attributeKey, Object value){
		if(target == null || attributeKey == null || value == null) return false;
		
		NodeAttributeTableModel attributes = AttributeController.getController().createAttributeTableModel(target);
		if(attributes != null){
			if(attributes.getAttributeKeyList().contains(TextUtils.getText(attributeKey))){
				//attributes.getAttribute(attributes.getAttributePosition(TextUtils.getText(attributeKey))).setValue(value);
				AttributeController.getController().performSetValueAt(attributes, value, attributes.getAttributePosition(TextUtils.getText(attributeKey)), 1);
				return true;
			}
			else{
				AttributeController.getController().performInsertRow(attributes, attributes.getRowCount(), TextUtils.getText(attributeKey), value); 
				return true;
			}
		}
		return false;	
	}
		
	public static void removeAttribute(NodeModel target, String attributeKey) {
		NodeAttributeTableModel attributes = AttributeController.getController().createAttributeTableModel(target);
		if(attributes != null && attributes.getAttributeKeyList().contains(TextUtils.getText(attributeKey))) {
			AttributeController.getController().performRemoveAttribute(TextUtils.getText(attributeKey));
		}
	}

	public static Object getAttributeValue(NodeModel target, String attributeKey) {
		if(target == null || attributeKey == null) return null;
		NodeAttributeTableModel attributes = AttributeController.getController().createAttributeTableModel(target);
		if(attributes != null){
			if(attributes.getAttributeKeyList().contains(TextUtils.getText(attributeKey))){
				return attributes.getAttribute(attributes.getAttributePosition(TextUtils.getText(attributeKey))).getValue();				
			}
		}
		return null;
	}

	public static boolean isAutoMonitorNode(NodeModel node) {
		if(NodeUtils.getAttributeValue(node, PdfUtilitiesController.MON_AUTO) == null) return false;
		int value = (Integer)NodeUtils.getAttributeValue(node, PdfUtilitiesController.MON_AUTO);
		switch(value){
			
			case 0:
				return false;				
				
			case 1:
				return true;				
				
			case 2:
				return ResourceController.getResourceController().getBooleanProperty("docear_auto_monitoring");
				
			default:
				return false;
		}
	}

}
