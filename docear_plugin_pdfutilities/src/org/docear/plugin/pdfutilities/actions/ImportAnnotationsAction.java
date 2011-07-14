package org.docear.plugin.pdfutilities.actions;

import java.net.URI;

import org.docear.plugin.pdfutilities.PdfFileFilter;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.link.NodeLinks;
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
			this.setEnabled(isPdfLinkedNode(selected));
		}
	}
	
	private boolean isPdfLinkedNode(NodeModel selected){
		URI link = NodeLinks.getLink(selected);		
        return new PdfFileFilter().accept(link);
    }	

}
