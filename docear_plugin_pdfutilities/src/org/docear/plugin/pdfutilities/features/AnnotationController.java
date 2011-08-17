package org.docear.plugin.pdfutilities.features;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.docear.plugin.pdfutilities.features.IAnnotation.AnnotationType;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.docear.plugin.pdfutilities.util.Tools;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

import de.intarsys.pdf.cos.COSRuntimeException;
import de.intarsys.pdf.parser.COSLoadException;

public class AnnotationController implements IExtension{
	
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
				if(!importedAnnotation.getTitle().equals(oldAnnotation.getTitle())){
					importedAnnotation.setConflicted(true);
					addConflictedAnnotation(oldAnnotation, result);
				}
				
			}
		}
		if(importedAnnotation.isConflicted()){
			addConflictedAnnotation(importedAnnotation, result);
		}
		for(AnnotationModel child : importedAnnotation.getChildren()){
			addConflictedAnnotations(getConflictedAnnotations(child, oldAnnotations), result);
		}
		return result;
	}
	
	public static AnnotationModel getModel(final NodeModel node, boolean update) {
		AnnotationModel annotation = (AnnotationModel) node.getExtension(AnnotationModel.class);
		if(annotation == null && update){
			return AnnotationController.setModel(node);
		}
		return annotation;
	}
	
	public static AnnotationNodeModel getAnnotationNodeModel(final NodeModel node){
		IAnnotation annotation = AnnotationController.getModel(node, true);
		if(annotation != null && annotation.getAnnotationType() != null && !annotation.getAnnotationType().equals(AnnotationType.PDF_FILE)){
			return new AnnotationNodeModel(node, new AnnotationID(Tools.getAbsoluteUri(node), annotation.getObjectNumber()), annotation.getAnnotationType());
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
	}
	
	private static AnnotationModel setModel(final NodeModel node){
		if(!NodeUtils.isPdfLinkedNode(node)){
			return null;
		}
		URI uri = Tools.getAbsoluteUri(node);	
		try {
			AnnotationModel model = new PdfAnnotationImporter().searchAnnotation(uri, node);			
			return model;
		} catch (COSRuntimeException e) {
			// TODO: DOCEAR exception handling ?			
		} catch (IOException e) {
			
		} catch (COSLoadException e) {
			
		}
		return null;
	}
	
	private static void addConflictedAnnotation(IAnnotation annotation, Map<AnnotationID, Collection<IAnnotation>> result){
		if(result.containsKey(annotation.getAnnotationID())){
			result.get(annotation.getAnnotationID()).add(annotation);
		}
		else{
			result.put(annotation.getAnnotationID(), new ArrayList<IAnnotation>());
			result.get(annotation.getAnnotationID()).add(annotation);
		}
	}
	
	private static void addConflictedAnnotations(Map<AnnotationID, Collection<IAnnotation>> conflicts, Map<AnnotationID, Collection<IAnnotation>> result){
		for(AnnotationID id :conflicts.keySet()){
			if(result.containsKey(id)){
				result.get(id).addAll(conflicts.get(id));
			}
			else{
				result.put(id, conflicts.get(id));
			}
		}
	}

}
