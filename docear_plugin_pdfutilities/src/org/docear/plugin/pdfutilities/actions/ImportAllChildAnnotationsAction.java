package org.docear.plugin.pdfutilities.actions;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.ArrayList;

import org.docear.plugin.core.features.AnnotationModel;
import org.docear.plugin.core.features.IAnnotation.AnnotationType;
import org.docear.plugin.core.util.Tools;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

@EnabledAction( checkOnPopup = true, checkOnNodeChange = true )
public class ImportAllChildAnnotationsAction extends ImportAnnotationsAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("serial")
	public ImportAllChildAnnotationsAction(String key) {
		super(key);
		this.setEnableType(new ArrayList<AnnotationType>(){{ add(AnnotationType.BOOKMARK); 
															 add(AnnotationType.BOOKMARK_WITH_URI);
															 add(AnnotationType.BOOKMARK_WITHOUT_DESTINATION);
														   }});
	}

	public void actionPerformed(ActionEvent evt) {
		NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		if(selected == null){
			return;
		}
		
		else{			
			PdfAnnotationImporter importer = new PdfAnnotationImporter();    
			URI uri = Tools.getAbsoluteUri(selected);
			try {
				AnnotationModel annotation = importer.searchAnnotation(uri, selected);				          
                NodeUtils.insertChildNodesFrom(annotation.getChildren(), selected.isLeft(), selected);
			} catch (Exception e) {
				LogUtils.severe("ImportAllChildAnnotationsAction Exception at URI("+uri+"): ", e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

	}

}
