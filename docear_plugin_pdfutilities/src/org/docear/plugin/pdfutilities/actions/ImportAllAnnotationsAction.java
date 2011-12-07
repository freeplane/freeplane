package org.docear.plugin.pdfutilities.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.docear.plugin.core.util.Tools;
import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.docear.plugin.pdfutilities.features.IAnnotation.AnnotationType;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

import de.intarsys.pdf.cos.COSRuntimeException;
import de.intarsys.pdf.parser.COSLoadException;

@EnabledAction( checkOnPopup = true, checkOnNodeChange = true )
public class ImportAllAnnotationsAction extends ImportAnnotationsAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;	

	@SuppressWarnings("serial")
	public ImportAllAnnotationsAction(String key) {
		super(key);
		this.setEnableType(new ArrayList<AnnotationType>(){{ add(AnnotationType.PDF_FILE); }});
	}

	public void actionPerformed(ActionEvent event) {
		NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		if(selected == null){
			return;
		}
		
		else{
			URI uri = Tools.getAbsoluteUri(selected);
            try {
            	PdfAnnotationImporter importer = new PdfAnnotationImporter();            	
				List<AnnotationModel> annotations = importer.importAnnotations(uri);
				NodeUtils nodeUtils = new NodeUtils();                
                nodeUtils.insertChildNodesFrom(annotations, selected.isLeft(), selected);
			} catch (IOException e) {
				LogUtils.severe("ImportAllAnnotationsAction IOException at URI("+uri+"): ", e);
			} catch (COSLoadException e) {
				LogUtils.severe("ImportAllAnnotationsAction COSLoadException at URI("+uri+"): ", e);
			} catch (COSRuntimeException e) {
				LogUtils.severe("ImportAllAnnotationsAction COSRuntimeException at URI("+uri+"): ", e);
			}
		}
	}
	
}
