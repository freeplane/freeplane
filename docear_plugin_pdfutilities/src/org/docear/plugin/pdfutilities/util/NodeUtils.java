package org.docear.plugin.pdfutilities.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docear.plugin.pdfutilities.features.AnnotationController;
import org.docear.plugin.pdfutilities.features.AnnotationID;
import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.docear.plugin.pdfutilities.features.AnnotationNodeModel;
import org.docear.plugin.pdfutilities.features.IAnnotation;
import org.docear.plugin.pdfutilities.features.IAnnotation.AnnotationType;
import org.docear.plugin.pdfutilities.pdf.PdfFileFilter;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;


public class NodeUtils {
	
	private final MMapController currentMapController;
	
	public NodeUtils(){
		this.currentMapController = (MMapController) Controller.getCurrentModeController().getMapController();
		
	}
	
	public Map<AnnotationID, Collection<AnnotationNodeModel>> getOldAnnotationsFromCurrentMap(){
		return getOldAnnotationsFrom(this.currentMapController.getRootNode());
	}
	
	private Map<AnnotationID, Collection<AnnotationNodeModel>> getOldAnnotationsFrom(NodeModel parent){
		Map<AnnotationID, Collection<AnnotationNodeModel>> result = new HashMap<AnnotationID, Collection<AnnotationNodeModel>>();
		
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

	public List<NodeModel> insertNewChildNodesFrom(Collection<AnnotationModel> annotations, boolean isLeft, NodeModel target) {
		List<NodeModel> nodes = new ArrayList<NodeModel>();
		
		for(AnnotationModel annotation : annotations){
			if(annotation.isNew() || annotation.hasNewChildren()){
				NodeModel equalChild = targetHasEqualChild(target, annotation);
				if(equalChild == null){
					NodeModel node = this.insertChildNodeFrom(annotation.getUri(), annotation, isLeft, target);
					this.insertNewChildNodesFrom(annotation.getChildren(), isLeft, node);
					nodes.add(node);
				}
				else{
					this.insertNewChildNodesFrom(annotation.getChildren(), isLeft, equalChild);
					nodes.add(equalChild);
				}
				
			}		
		}
		
		return nodes;
	}
	
	public NodeModel targetHasEqualChild(NodeModel target, IAnnotation annotation){
		for(NodeModel child : target.getChildren()){
			IAnnotation oldAnnotation = AnnotationController.getAnnotationNodeModel(child);
			if(oldAnnotation != null && oldAnnotation.getAnnotationID().equals(annotation.getAnnotationID())){
				return child;
			}
		}
		return null;
	}

}
