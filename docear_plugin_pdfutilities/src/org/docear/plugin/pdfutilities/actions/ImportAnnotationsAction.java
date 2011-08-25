package org.docear.plugin.pdfutilities.actions;

import java.util.ArrayList;
import java.util.List;

import org.docear.plugin.pdfutilities.features.AnnotationController;
import org.docear.plugin.pdfutilities.features.AnnotationNodeModel;
import org.docear.plugin.pdfutilities.features.IAnnotation.AnnotationType;
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
						break;
					}
					else{
						this.setEnabled(false);
					}
				}
				
			}
			else{
				this.setEnabled(false);
			}
		}
		this.setVisible(this.isEnabled());
	}

	public List<AnnotationType> getEnableTypes() {
		return enableTypes;
	}

	public void setEnableType(List<AnnotationType> enableTypes) {
		this.enableTypes.clear();
		this.enableTypes.addAll(enableTypes);
	}
	
	
		

}
