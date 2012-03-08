package org.docear.plugin.pdfutilities.actions;

import java.awt.event.ActionEvent;
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

@EnabledAction( checkOnPopup = true, checkOnNodeChange = true )
public class ImportNewChildAnnotationsAction extends ImportAnnotationsAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("serial")
	public ImportNewChildAnnotationsAction(String key) {
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
				Map<AnnotationID, Collection<AnnotationNodeModel>> oldAnnotations = NodeUtils.getOldAnnotationsFromCurrentMap();				
				Collection<AnnotationModel> annotations = AnnotationController.markNewAnnotations(annotation.getChildren(), oldAnnotations);
				Map<AnnotationID, Collection<IAnnotation>> conflicts = AnnotationController.getConflictedAnnotations(annotations, oldAnnotations);
				if(conflicts.size() > 0){
					ImportConflictDialog dialog = new ImportConflictDialog(Controller.getCurrentController().getViewController().getJFrame(), conflicts);
					dialog.showDialog();
				}
				System.gc();
				NodeUtils.insertNewChildNodesFrom(annotations, selected.isLeft(), selected, selected);
			} catch (Exception e) {
				LogUtils.severe("ImportAllChildAnnotationsAction Exception at URI("+uri+"): ", e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

}
