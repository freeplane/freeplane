package org.docear.plugin.pdfutilities.actions;

import java.util.ArrayList;
import java.util.List;

import org.docear.plugin.core.features.AnnotationNodeModel;
import org.docear.plugin.core.features.IAnnotation.AnnotationType;
import org.docear.plugin.core.mindmap.AnnotationController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

public abstract class ImportAnnotationsAction extends DocearAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<AnnotationType> enableTypes = new ArrayList<AnnotationType>();

	public ImportAnnotationsAction(String key) {
		super(key);
	}
	
	public void setEnabled(){
		if(Controller.getCurrentController().getSelection() == null) {
			this.setEnabled(false);
			this.setVisible(false);
			return;
		}
		NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		if(selected == null){
			this.setEnabled(false);
		}
		else{
			AnnotationNodeModel model = AnnotationController.getAnnotationNodeModel(selected);
			if(model != null && model.getAnnotationType() != null){	
				for(AnnotationType enableType : this.getEnableTypes()){
					if(model.getAnnotationType().equals(enableType)){
						this.setEnabled(true);
						this.setVisible(true);
						break;
					}
					else{
						this.setEnabled(false);
						this.setVisible(false);
					}
				}
				
			}
			else{
				for(AnnotationType enableType : this.getEnableTypes()){
					if(enableType.equals(AnnotationType.PDF_FILE)){
						this.setEnabled(false);
						this.setVisible(true);
						break;
					}
					else{
						this.setEnabled(false);
						this.setVisible(false);
					}
				}				
			}
		}		
	}

	public List<AnnotationType> getEnableTypes() {
		return enableTypes;
	}

	public void setEnableType(List<AnnotationType> enableTypes) {
		this.enableTypes.clear();
		this.enableTypes.addAll(enableTypes);
	}
	
	
		

}
