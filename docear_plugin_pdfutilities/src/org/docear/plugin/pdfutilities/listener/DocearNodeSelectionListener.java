package org.docear.plugin.pdfutilities.listener;

import org.docear.plugin.pdfutilities.features.AnnotationController;
import org.docear.plugin.pdfutilities.features.IAnnotation;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

public class DocearNodeSelectionListener implements INodeSelectionListener {

	public void onDeselect(NodeModel node) {
		Controller.getCurrentController().getViewController().removeStatus("Annotation Info");
	}

	public void onSelect(NodeModel node) {
		IAnnotation model = AnnotationController.getModel(node, false);
		
		if(model != null){
			StringBuilder builder = new StringBuilder();
			
			if(model.getAnnotationType() != null){
				builder.append("Annotation Type: " + model.getAnnotationType());
			}
			if(model.getPage() != null){
				builder.append(" Page: " + model.getPage());
			}
			
			if(model.getObjectNumber() != null){
				builder.append(" Object Number: " + model.getObjectNumber());
			}
			
			if(model.getGenerationNumber() != null){
				builder.append(" Generation Number: " + model.getGenerationNumber());
			}
			
			if(model.getAnnotationID() != null){
				builder.append(" AnnotationID: " + model.getAnnotationID().getId());
			}
			
			Controller.getCurrentController().getViewController().addStatusInfo("Annotation Info", builder.toString());
		}
	}

}
