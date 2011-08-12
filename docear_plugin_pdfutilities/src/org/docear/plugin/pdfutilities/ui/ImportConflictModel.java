package org.docear.plugin.pdfutilities.ui;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.docear.plugin.pdfutilities.features.PdfAnnotationExtensionModel;
import org.docear.plugin.pdfutilities.util.Tools;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.NodeModel;



public class ImportConflictModel {
	
	private Map<URI , Map<Integer, Collection<AnnotationConflictModel>>> importConflicts = new HashMap<URI , Map<Integer, Collection<AnnotationConflictModel>>>();
	
	public ImportConflictModel(Collection<PdfAnnotationExtensionModel> importedAnnotations, Map<URI, Collection<NodeModel>> pdfLinkedNodes){
		this.createModel(importedAnnotations, pdfLinkedNodes);
	}
	
	public boolean hasConflicts(){
		return this.importConflicts.keySet().size() > 0;
	}
	
	public Collection<URI> getUrisFromConflictedPdfs(){
		return this.importConflicts.keySet();
	}
	
	public Map<Integer, Collection<AnnotationConflictModel>> getConflictedAnnotations(URI uri){		
		if(this.importConflicts.containsKey(uri)){
			return  this.importConflicts.get(uri);
		}
		return new HashMap<Integer, Collection<AnnotationConflictModel>>();
	}

	private void createModel(Collection<PdfAnnotationExtensionModel> importedAnnotations, Map<URI, Collection<NodeModel>> pdfLinkedNodes) {
		for(PdfAnnotationExtensionModel annotation : importedAnnotations){
			this.createModel(annotation.getChildren(), pdfLinkedNodes);
			
			this.createModel(annotation, pdfLinkedNodes);
		}		
	}

	private void createModel(PdfAnnotationExtensionModel importedAnnotation, Map<URI, Collection<NodeModel>> pdfLinkedNodes) {
		if(pdfLinkedNodes.containsKey(importedAnnotation.getAbsoluteUri())){
			for(NodeModel node : pdfLinkedNodes.get(importedAnnotation.getAbsoluteUri())){
				PdfAnnotationExtensionModel model = PdfAnnotationExtensionModel.getModel(node);
				
				if(model == null){
					model = PdfAnnotationExtensionModel.setModel(node);
				}				
				if(model == null || model.getObjectNumber() == null || importedAnnotation.getObjectNumber() == null){
					//TODO: DOCEAR Cannot set conflicted state without model
				}
				if(model != null && model.getObjectNumber() != null && importedAnnotation.getObjectNumber() != null){
					if(model.getObjectNumber().equals(importedAnnotation.getObjectNumber()) && !importedAnnotation.getTitle().equals(node.getText())){
						importedAnnotation.setConflicted(true);
						this.add(node);
					}
				}
			}
		}
		if(importedAnnotation.isConflicted()){
			this.add(importedAnnotation);
		}
		
	}

	private void add(PdfAnnotationExtensionModel importedAnnotation) {
		URI uri = importedAnnotation.getAbsoluteUri();
				
		Map<Integer, Collection<AnnotationConflictModel>> pdfConflicts = null;
		if(this.importConflicts.containsKey(uri)){
			pdfConflicts = this.importConflicts.get(uri);
		}
		else{
			pdfConflicts = this.importConflicts.put(uri, new HashMap<Integer, Collection<AnnotationConflictModel>>());
		}
		
		Collection<AnnotationConflictModel> annotationConflicts = null;
		if(pdfConflicts.containsKey(importedAnnotation.getObjectNumber())){
			annotationConflicts = pdfConflicts.get(importedAnnotation.getObjectNumber());
		}
		else{
			annotationConflicts = pdfConflicts.put(importedAnnotation.getObjectNumber(), new ArrayList<AnnotationConflictModel>());
		}
		//TODO: DOCEAR use Map instead of Collection ??
		
		annotationConflicts.add(new AnnotationConflictModel(importedAnnotation));		
	}

	private void add(NodeModel node) {
		URI uri = Tools.getAbsoluteUri(NodeLinks.getValidLink(node));
		PdfAnnotationExtensionModel model = PdfAnnotationExtensionModel.getModel(node);
		
		Map<Integer, Collection<AnnotationConflictModel>> pdfConflicts = null;
		if(this.importConflicts.containsKey(uri)){
			pdfConflicts = this.importConflicts.get(uri);
		}
		else{
			pdfConflicts = this.importConflicts.put(uri, new HashMap<Integer, Collection<AnnotationConflictModel>>());
		}
		
		Collection<AnnotationConflictModel> annotationConflicts = null;
		if(pdfConflicts.containsKey(model.getObjectNumber())){
			annotationConflicts = pdfConflicts.get(model.getObjectNumber());
		}
		else{
			annotationConflicts = pdfConflicts.put(model.getObjectNumber(), new ArrayList<AnnotationConflictModel>());
		}
		//TODO: DOCEAR use Map instead of Collection ??
		
		annotationConflicts.add(new AnnotationConflictModel(node));		
	}

}
