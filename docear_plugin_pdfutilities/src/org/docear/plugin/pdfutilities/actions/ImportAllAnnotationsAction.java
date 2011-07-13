package org.docear.plugin.pdfutilities.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.docear.plugin.pdfutilities.NodeUtils;
import org.docear.plugin.pdfutilities.PdfAnnotation;
import org.docear.plugin.pdfutilities.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.PdfFileFilter;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

import de.intarsys.pdf.parser.COSLoadException;

@EnabledAction( checkOnNodeChange = true )
public class ImportAllAnnotationsAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImportAllAnnotationsAction(String key) {
		super(key);		
	}

	public void actionPerformed(ActionEvent event) {
		NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		if(selected == null){
			return;
		}
		
		else{
			URI uri = NodeLinks.getLink(selected);
            try {
            	PdfAnnotationImporter importer = new PdfAnnotationImporter();            	
				List<PdfAnnotation> annotations = importer.importAnnotations(uri);
				NodeUtils nodeUtils = new NodeUtils();                
                nodeUtils.insertChildNodesFrom(annotations, selected.isLeft(), selected);
			} catch (IOException e) {
				LogUtils.severe("ImportAllAnnotationsAction IOException at URI("+uri+"): ", e);
			} catch (COSLoadException e) {
				LogUtils.severe("ImportAllAnnotationsAction ImportException at URI("+uri+"): ", e);
			}
		}
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
