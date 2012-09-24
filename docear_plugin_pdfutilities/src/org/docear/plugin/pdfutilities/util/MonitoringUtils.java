package org.docear.plugin.pdfutilities.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.docear.plugin.core.CoreConfiguration;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.features.AnnotationID;
import org.docear.plugin.core.util.NodeUtilities;
import org.docear.plugin.core.util.Tools;
import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.docear.plugin.pdfutilities.features.AnnotationNodeModel;
import org.docear.plugin.pdfutilities.features.IAnnotation;
import org.docear.plugin.pdfutilities.features.IAnnotation.AnnotationType;
import org.docear.plugin.pdfutilities.map.AnnotationController;
import org.docear.plugin.pdfutilities.pdf.PdfFileFilter;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;

public abstract class MonitoringUtils {
	
	public static boolean isMonitoringNode(NodeModel node) {
		NodeAttributeTableModel attributeModel = (NodeAttributeTableModel) node.getExtension(NodeAttributeTableModel.class);
		return (attributeModel != null && attributeModel.getAttributeKeyList().contains(PdfUtilitiesController.MON_INCOMING_FOLDER));
	}

	public static URI getPdfDirFromMonitoringNode(NodeModel node) {
		if(!isMonitoringNode(node)) return null;
		NodeAttributeTableModel attributeModel = (NodeAttributeTableModel) node.getExtension(NodeAttributeTableModel.class);
		if(attributeModel == null || !attributeModel.getAttributeKeyList().contains(PdfUtilitiesController.MON_INCOMING_FOLDER)){
			return null;
		}
		
		Object value  = attributeModel.getValue(attributeModel.getAttributePosition(PdfUtilitiesController.MON_INCOMING_FOLDER));
		if (value instanceof String) {
			try {
				value = new URI((String) value);
			}
			catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		
		if(value.toString().equals(CoreConfiguration.DOCUMENT_REPOSITORY_PATH)){
			return CoreConfiguration.repositoryPathObserver.getUri();
			
		}
		else{			
			return Tools.getAbsoluteUri((URI)value);
		}
	}
	
	public static List<URI> getMindmapDirFromMonitoringNode(NodeModel node) {
		List<URI> result = new ArrayList<URI>();
		if(!isMonitoringNode(node)) return result;
		NodeAttributeTableModel attributeModel = (NodeAttributeTableModel) node.getExtension(NodeAttributeTableModel.class);
		if(attributeModel == null || !attributeModel.getAttributeKeyList().contains(PdfUtilitiesController.MON_MINDMAP_FOLDER)){
			return result;
		}
		
		Object value = attributeModel.getValue(attributeModel.getAttributePosition(PdfUtilitiesController.MON_MINDMAP_FOLDER));
		
		if(value.toString().equals(CoreConfiguration.LIBRARY_PATH)){
			return DocearController.getController().getLibrary().getMindmaps();			
		}
		else{			
			result.add(Tools.getAbsoluteUri((URI)value));
			return result;
		}		
	}

	public static void removeMonitoringEntries(NodeModel selected) {
		NodeAttributeTableModel attributeModel = (NodeAttributeTableModel) selected.getExtension(NodeAttributeTableModel.class);
		if(attributeModel == null) return;
		
		if(attributeModel.getAttributeKeyList().contains(PdfUtilitiesController.MON_INCOMING_FOLDER)){
			AttributeController.getController(MModeController.getMModeController()).performRemoveRow(attributeModel, attributeModel.getAttributePosition(PdfUtilitiesController.MON_INCOMING_FOLDER));			
		}
		
		if(attributeModel.getAttributeKeyList().contains(PdfUtilitiesController.MON_MINDMAP_FOLDER)){
			AttributeController.getController(MModeController.getMModeController()).performRemoveRow(attributeModel, attributeModel.getAttributePosition(PdfUtilitiesController.MON_MINDMAP_FOLDER));			
		}
		
		if(attributeModel.getAttributeKeyList().contains(PdfUtilitiesController.MON_AUTO)){
			AttributeController.getController(MModeController.getMModeController()).performRemoveRow(attributeModel, attributeModel.getAttributePosition(PdfUtilitiesController.MON_AUTO));			
		}
		
		if(attributeModel.getAttributeKeyList().contains(PdfUtilitiesController.MON_SUBDIRS)){
			AttributeController.getController(MModeController.getMModeController()).performRemoveRow(attributeModel, attributeModel.getAttributePosition(PdfUtilitiesController.MON_SUBDIRS));			
		}
		
		if(attributeModel.getAttributeKeyList().contains(PdfUtilitiesController.MON_FLATTEN_DIRS)){
			AttributeController.getController(MModeController.getMModeController()).performRemoveRow(attributeModel, attributeModel.getAttributePosition(PdfUtilitiesController.MON_FLATTEN_DIRS));			
		}
	}
	
	public static boolean isAutoMonitorNode(NodeModel node) {
		if(NodeUtilities.getAttributeValue(node, PdfUtilitiesController.MON_AUTO) == null) return false;
		Integer value = NodeUtilities.getAttributeIntValue(node, PdfUtilitiesController.MON_AUTO);	
		
		switch(value){
			
			case 0:
				return false;				
				
			case 1:
				return true;				
				
			case 2:
				return ResourceController.getResourceController().getBooleanProperty("docear_auto_monitoring"); //$NON-NLS-1$
				
			default:
				return false;
		}
	}
	
	public static Stack<File> getFolderStructureStack(NodeModel monitoringNode, URI pdfFile){
		Stack<File> folderStack = new Stack<File>();		
		URI pdfDirURI = getPdfDirFromMonitoringNode(monitoringNode);
		pdfDirURI = Tools.getAbsoluteUri(pdfDirURI);
		if(pdfDirURI == null || Tools.getFilefromUri(pdfDirURI) == null || !Tools.getFilefromUri(pdfDirURI).exists() || !Tools.getFilefromUri(pdfDirURI).isDirectory()){
			return folderStack;
		}
		File pdfDirFile = Tools.getFilefromUri(pdfDirURI);		
		File parent = Tools.getFilefromUri(pdfFile).getParentFile();
		while(parent != null && !parent.equals(pdfDirFile)){
			folderStack.push(parent);
			parent = parent.getParentFile();
			if(parent == null){
				folderStack.clear();
			}
		}
		return folderStack;
	}
	
	public static boolean isPdfLinkedNode(NodeModel node){
		URI link = Tools.getAbsoluteUri(node);		
        return new PdfFileFilter().accept(link);
    }
	

	public static List<NodeModel> insertNewChildNodesFrom(URI pdfFile, Collection<AnnotationModel> annotations, boolean isLeft, boolean flattenSubfolder, NodeModel target){
		AnnotationModel root = new AnnotationModel(new AnnotationID(Tools.getAbsoluteUri(pdfFile), 0), AnnotationType.PDF_FILE);
		root.setTitle(Tools.getFilefromUri(Tools.getAbsoluteUri(pdfFile)).getName());
		root.getChildren().addAll(annotations);
		Collection<AnnotationModel> newList = new ArrayList<AnnotationModel>();
		newList.add(root);
		if(!flattenSubfolder){		
			Stack<File> folderStack = getFolderStructureStack(target, pdfFile);
			target = NodeUtilities.createFolderStructurePath(target, folderStack);
		}
		return insertNewChildNodesFrom(newList, isLeft, target, target);
	}
	
	public static Map<AnnotationID, Collection<AnnotationNodeModel>> getOldAnnotationsFromMaps(Collection<URI> mindmaps){
		Map<AnnotationID, Collection<AnnotationNodeModel>> result = new HashMap<AnnotationID, Collection<AnnotationNodeModel>>();
		for(MapModel map : NodeUtilities.getMapsFromUris(mindmaps)){
			
			Map<AnnotationID, Collection<AnnotationNodeModel>> temp = getOldAnnotationsFrom(map.getRootNode());
			for(AnnotationID id : temp.keySet()){
				if(!result.containsKey(id)){
					result.put(id, new ArrayList<AnnotationNodeModel>());				
				}
				result.get(id).addAll(temp.get(id));
			}
		} 
		return result;
	}
	
	public static Map<AnnotationID, Collection<AnnotationNodeModel>> getOldAnnotationsFromCurrentMap(){
		return getOldAnnotationsFrom(((MMapController) Controller.getCurrentModeController().getMapController()).getRootNode());
	}
	
	private static Map<AnnotationID, Collection<AnnotationNodeModel>> getOldAnnotationsFrom(NodeModel parent){
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
	
	public static Map<AnnotationID, Collection<AnnotationNodeModel>> getOldAnnotationsFromMap(URI mindmap){
		MapModel map = NodeUtilities.getMapFromUri(mindmap);
		if(map != null){
			return getOldAnnotationsFrom(map.getRootNode());
		}
		return new HashMap<AnnotationID, Collection<AnnotationNodeModel>>();
	}
	
	public static NodeModel insertChildNodesFromPdf(URI pdfFile, List<AnnotationModel> annotations, boolean isLeft, NodeModel target){
		NodeModel node = insertChildNodeFrom(pdfFile, isLeft, target, AnnotationType.PDF_FILE);
		insertChildNodesFrom(annotations, isLeft, node);
		return node;
	}
	
	public static List<NodeModel> insertChildNodesFrom(List<AnnotationModel> annotations, boolean isLeft, NodeModel target){
		List<NodeModel> nodes = new ArrayList<NodeModel>();
		
		for(AnnotationModel annotation : annotations){
			NodeModel node = insertChildNodeFrom(annotation.getUri(), annotation, isLeft, target);
			insertChildNodesFrom(annotation.getChildren(), isLeft, node);
			nodes.add(node);
		}
		
		return nodes;
	}
	
	public static NodeModel insertChildNodeFrom(URI file, boolean isLeft, NodeModel target, AnnotationType type){
		final NodeModel node = ((MMapController) Controller.getCurrentModeController().getMapController()).newNode(Tools.getFilefromUri(file).getName(), target.getMap());
		
		
		if(type != null){
			IAnnotation model = new AnnotationModel();
			model.setAnnotationType(type);
			AnnotationController.setModel(node, model);
		}
		NodeUtilities.setLinkFrom(file, node);
		return NodeUtilities.insertChildNodeFrom(node, isLeft, target);
	}
	
	public static NodeModel insertChildNodeFrom(URI file, IAnnotation annotation, boolean isLeft, NodeModel target){	
		if(annotation.getTitle() != null && annotation.getTitle().length() > 1 && annotation.getTitle().charAt(0) == '='){
			annotation.setTitle(" " + annotation.getTitle()); //$NON-NLS-1$
		}
		final NodeModel node = ((MMapController) Controller.getCurrentModeController().getMapController()).newNode(annotation.getTitle(), target.getMap());
		AnnotationController.setModel(node, annotation);
		NodeUtilities.setLinkFrom(file, node);
				
		return NodeUtilities.insertChildNodeFrom(node, isLeft, target);
	}
	
	public static List<NodeModel> insertNewChildNodesFrom(Collection<AnnotationModel> annotations, boolean isLeft, NodeModel target, NodeModel rootTarget) {
		List<NodeModel> nodes = new ArrayList<NodeModel>();
		
		for(AnnotationModel annotation : annotations){			
			if(annotation.isNew() || annotation.hasNewChildren()){
				NodeModel equalChild = targetHasEqualChild(rootTarget, annotation);
				
				if(equalChild == null){
					NodeModel node = insertChildNodeFrom(annotation.getUri(), annotation, isLeft, target);
					insertNewChildNodesFrom(annotation.getChildren(), isLeft, node, rootTarget);
					nodes.add(node);
				}
				else{
					insertNewChildNodesFrom(annotation.getChildren(), isLeft, equalChild, rootTarget);
					nodes.add(equalChild);
				}
				
			}		
		}
		
		return nodes;
	}
	
	public static NodeModel targetHasEqualChild(NodeModel target, IAnnotation annotation){
		if(annotation == null)	return null;
		
		for(NodeModel child : target.getChildren()){
			IAnnotation oldAnnotation = AnnotationController.getAnnotationNodeModel(child);
			NodeModel equalChild = targetHasEqualChild(child, annotation);
			if(equalChild != null) {
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


}
