package org.docear.plugin.core.mindmap;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.docear.plugin.core.features.AnnotationID;
import org.docear.plugin.core.features.AnnotationModel;
import org.docear.plugin.core.features.AnnotationNodeModel;
import org.docear.plugin.core.features.AnnotationXmlBuilder;
import org.docear.plugin.core.features.IAnnotation;
import org.docear.plugin.core.features.IAnnotation.AnnotationType;
import org.docear.plugin.core.util.Tools;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.WorkspaceUtils;

public class AnnotationController implements IExtension{
	
	private final static Set<IAnnotationImporter> annotationImporters = new HashSet<IAnnotationImporter>();
	
	public static void addAnnotationImporter(IAnnotationImporter annotationImporter) {
		annotationImporters.add(annotationImporter);
	}
	
	public static Set<IAnnotationImporter> getAnnotationImporters() {
		return annotationImporters; 
	}
	
	public static AnnotationController getController() {
		return getController(Controller.getCurrentModeController());
	}

	public static AnnotationController getController(ModeController modeController) {
		return (AnnotationController) modeController.getExtension(AnnotationController.class);
	}
	public static void install( final AnnotationController annotationController) {
		Controller.getCurrentModeController().addExtension(AnnotationController.class, annotationController);
	}
	
	public AnnotationController(final ModeController modeController){
		final MapController mapController = Controller.getCurrentModeController().getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		AnnotationXmlBuilder builder = new AnnotationXmlBuilder();
		builder.registerBy(readManager, writeManager);
	}
	
	public static void markNewAnnotations(AnnotationModel importedAnnotation, Map<AnnotationID, Collection<AnnotationNodeModel>> oldAnnotations){
		for(AnnotationModel child : importedAnnotation.getChildren()){
			AnnotationController.markNewAnnotations(child, oldAnnotations);			
		}
		if(oldAnnotations.containsKey(importedAnnotation.getAnnotationID())){						
			importedAnnotation.setNew(false);			
		}
		else{
			importedAnnotation.setNew(true);
		}		
	}
	
	public static Collection<AnnotationModel> markNewAnnotations(Collection<AnnotationModel> importedAnnotations, Map<AnnotationID, Collection<AnnotationNodeModel>> oldAnnotations){
		for(AnnotationModel annotation : importedAnnotations){
			AnnotationController.markNewAnnotations(annotation, oldAnnotations);
		}
		return importedAnnotations;
	}
	
	public static Map<AnnotationID, Collection<IAnnotation>> getConflictedAnnotations(Collection<AnnotationModel> importedAnnotations, Map<AnnotationID, Collection<AnnotationNodeModel>> oldAnnotations) {
		Map<AnnotationID, Collection<IAnnotation>> result = new HashMap<AnnotationID, Collection<IAnnotation>>();
		for(AnnotationModel annotation : importedAnnotations){
			addConflictedAnnotations(getConflictedAnnotations(annotation, oldAnnotations), result);		
		}
		return result;
	}

	public static Map<AnnotationID, Collection<IAnnotation>> getConflictedAnnotations(AnnotationModel importedAnnotation, Map<AnnotationID, Collection<AnnotationNodeModel>> oldAnnotations) {
		Map<AnnotationID, Collection<IAnnotation>> result = new HashMap<AnnotationID, Collection<IAnnotation>>();
		if(oldAnnotations.containsKey(importedAnnotation.getAnnotationID())){
			for(AnnotationNodeModel oldAnnotation : oldAnnotations.get(importedAnnotation.getAnnotationID())){
				if(!importedAnnotation.getTitle().equals(oldAnnotation.getTitle()) && !oldAnnotation.getAnnotationType().equals(AnnotationType.PDF_FILE)){
					importedAnnotation.setConflicted(true);					
				}
				
			}
		}
		if(importedAnnotation.isConflicted()){
			addConflictedAnnotation(importedAnnotation, result);
			for(AnnotationNodeModel oldAnnotation : oldAnnotations.get(importedAnnotation.getAnnotationID())){
				addConflictedAnnotation(oldAnnotation, result);
			}
		}
		for(AnnotationModel child : importedAnnotation.getChildren()){
			addConflictedAnnotations(getConflictedAnnotations(child, oldAnnotations), result);
		}
		return result;
	}
	
	public static AnnotationModel getModel(final NodeModel node, boolean update) {
		AnnotationModel annotation = (AnnotationModel) node.getExtension(AnnotationModel.class);
		if(annotation == null && update){
			AnnotationController.setModel(node);
			annotation = (AnnotationModel) node.getExtension(AnnotationModel.class);
		}
		return annotation;
	}
	
	public static int getAnnotationPosition(NodeModel node){
		AnnotationModel annotation = AnnotationController.getModel(node, false);
    	if(annotation != null && annotation.getParent() != null){
    		return annotation.getParent().getChildIndex(annotation);
    	}
    	return -1;
	}
	
	private static boolean isPdfFile(File file) {
		if (file == null) {
			return false;
		}
		return file.getName().toLowerCase().endsWith(".pdf");
	}
	
	public static AnnotationNodeModel getAnnotationNodeModel(final NodeModel node){
		IAnnotation annotation = AnnotationController.getModel(node, true);
		File file = WorkspaceUtils.resolveURI(NodeLinks.getValidLink(node), node.getMap());
		if(annotation != null && file == null){
			setModel(node, null);
			return null;
		}
		if(annotation != null && annotation.getAnnotationType() != null && !annotation.getAnnotationType().equals(AnnotationType.PDF_FILE)){
			return new AnnotationNodeModel(node, new AnnotationID(Tools.getAbsoluteUri(node), annotation.getObjectNumber()), annotation.getAnnotationType());
		}		
		if(annotation != null && file != null && annotation.getAnnotationType().equals(AnnotationType.PDF_FILE)){
			return new AnnotationNodeModel(node, new AnnotationID(Tools.getAbsoluteUri(node), 0), AnnotationType.PDF_FILE); 
		}		
		if(annotation == null && file != null && file.getName().equals(node.getText()) && isPdfFile(file)){
			return new AnnotationNodeModel(node, new AnnotationID(Tools.getAbsoluteUri(node), 0), AnnotationType.PDF_FILE); 
		}
		if(annotation == null && file != null && file.getName().equals(node.getText()) && !isPdfFile(file)){
			return new AnnotationNodeModel(node, new AnnotationID(Tools.getAbsoluteUri(node), 0), AnnotationType.FILE); 
		}
		return null;
	}

	public static IAnnotation createModel(final NodeModel node) {
		final IAnnotation extension = (IAnnotation) node.getExtension(IAnnotation.class);
		if (extension != null) {
			return extension;
		}
		final IAnnotation annotationModel = new AnnotationModel();
		node.addExtension(annotationModel);
		return annotationModel;		
	}

	public static void setModel(final NodeModel node, final IAnnotation annotationModel) {
		final IAnnotation oldAnnotationModel = (IAnnotation) node.getExtension(IAnnotation.class);
		if (annotationModel != null && oldAnnotationModel == null) {
			node.addExtension(annotationModel);
		}
		else if (annotationModel == null && oldAnnotationModel != null) {
			node.removeExtension(AnnotationModel.class);
		}
		else if(annotationModel == null && oldAnnotationModel == null){
			node.removeExtension(AnnotationModel.class);
		}
	}
	
	private static void setModel(final NodeModel node){
		File file = WorkspaceUtils.resolveURI(NodeLinks.getValidLink(node), node.getMap());
		if(!isPdfFile(file)){
			return;
		}
		URI uri = Tools.getAbsoluteUri(node);
		for(IAnnotationImporter importer : annotationImporters) {
			try {			
				importer.searchAnnotation(uri, node);
			
			} catch (Exception e) {			
				LogUtils.warn(e.getMessage());
			}
		}		
	}
	
	public static void addConflictedAnnotation(IAnnotation annotation, Map<AnnotationID, Collection<IAnnotation>> result){
		if(result.containsKey(annotation.getAnnotationID())){
			boolean add = true;
			for(IAnnotation conflict: result.get(annotation.getAnnotationID())){
				if(annotation instanceof AnnotationModel && !(annotation instanceof AnnotationNodeModel) && conflict instanceof AnnotationModel){
					add = false;
					break;
				}
				if(annotation instanceof AnnotationNodeModel && conflict instanceof AnnotationNodeModel && ((AnnotationNodeModel) annotation).getNode().equals(((AnnotationNodeModel) conflict).getNode())){
					add = false;
					break;
				}
			}
			if(add){
				result.get(annotation.getAnnotationID()).add(annotation);
			}
		}
		else{
			result.put(annotation.getAnnotationID(), new ArrayList<IAnnotation>());
			result.get(annotation.getAnnotationID()).add(annotation);
		}
	}
	
	public static void addConflictedAnnotations(Map<AnnotationID, Collection<IAnnotation>> conflicts, Map<AnnotationID, Collection<IAnnotation>> result){
		for(AnnotationID id :conflicts.keySet()){
			if(result.containsKey(id)){
				for(IAnnotation conflict : conflicts.get(id)){
					addConflictedAnnotation(conflict, result);
				}
				//result.get(id).addAll(conflicts.get(id));
			}
			else{
				result.put(id, conflicts.get(id));
			}
		}
	}

	

}
