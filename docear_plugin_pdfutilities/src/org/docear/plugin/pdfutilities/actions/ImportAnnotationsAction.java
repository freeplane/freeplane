package org.docear.plugin.pdfutilities.actions;

import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

public abstract class ImportAnnotationsAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImportAnnotationsAction(String key) {
		super(key);
	}
	
	public void setEnabled(){
		NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		if(selected == null){
			this.setEnabled(false);
		}
		else{
			this.setEnabled(NodeUtils.isPdfLinkedNode(selected));
		}
	}
		

}
