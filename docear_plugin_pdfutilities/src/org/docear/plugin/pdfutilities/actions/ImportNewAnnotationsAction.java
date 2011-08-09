package org.docear.plugin.pdfutilities.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Map;

import org.docear.plugin.pdfutilities.features.PdfAnnotationExtensionModel;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

import de.intarsys.pdf.parser.COSLoadException;

@EnabledAction( checkOnNodeChange = true )
public class ImportNewAnnotationsAction extends ImportAnnotationsAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImportNewAnnotationsAction(String key) {
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
				Collection<PdfAnnotationExtensionModel> annotations = importer.importAnnotations(uri);
				NodeUtils nodeUtils = new NodeUtils();
				Map<URI, Collection<NodeModel>> pdfLinkedNodes = nodeUtils.getPdfLinkedNodesFromCurrentMap();
				annotations = PdfAnnotationExtensionModel.markNewAnnotations(annotations, pdfLinkedNodes);
                nodeUtils.insertNewChildNodesFrom(annotations, selected.isLeft(), selected);
			} catch (IOException e) {
				LogUtils.severe("ImportAllAnnotationsAction IOException at URI("+uri+"): ", e);
			} catch (COSLoadException e) {
				LogUtils.severe("ImportAllAnnotationsAction ImportException at URI("+uri+"): ", e);
			}	
		}

	}

}
