package org.docear.plugin.pdfutilities.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.docear.plugin.core.features.AnnotationID;
import org.docear.plugin.core.features.AnnotationModel;
import org.docear.plugin.core.features.AnnotationNodeModel;
import org.docear.plugin.core.features.IAnnotation;
import org.docear.plugin.core.features.IAnnotation.AnnotationType;
import org.docear.plugin.core.mindmap.AnnotationController;
import org.docear.plugin.core.util.Tools;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.ui.conflict.ImportConflictDialog;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

import de.intarsys.pdf.parser.COSLoadException;

@EnabledAction( checkOnPopup = true, checkOnNodeChange = true )
public class ImportNewAnnotationsAction extends ImportAnnotationsAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("serial")
	public ImportNewAnnotationsAction(String key) {
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
				Collection<AnnotationModel> annotations = importer.importAnnotations(uri);				
				Map<AnnotationID, Collection<AnnotationNodeModel>> oldAnnotations = NodeUtils.getOldAnnotationsFromCurrentMap();				
				annotations = AnnotationController.markNewAnnotations(annotations, oldAnnotations);
				Map<AnnotationID, Collection<IAnnotation>> conflicts = AnnotationController.getConflictedAnnotations(annotations, oldAnnotations);
				if(conflicts.size() > 0){
					ImportConflictDialog dialog = new ImportConflictDialog(Controller.getCurrentController().getViewController().getJFrame(), conflicts);
					dialog.showDialog();
				}
				
                NodeUtils.insertNewChildNodesFrom(annotations, selected.isLeft(), selected, selected);
			} catch (IOException e) {
				LogUtils.severe("ImportAllAnnotationsAction IOException at URI("+uri+"): ", e); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (COSLoadException e) {
				LogUtils.severe("ImportAllAnnotationsAction ImportException at URI("+uri+"): ", e); //$NON-NLS-1$ //$NON-NLS-2$
			}	
		}

	}

}
