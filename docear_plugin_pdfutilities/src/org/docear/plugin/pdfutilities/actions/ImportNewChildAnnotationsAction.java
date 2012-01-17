package org.docear.plugin.pdfutilities.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.docear.plugin.core.util.Tools;
import org.docear.plugin.pdfutilities.features.AnnotationController;
import org.docear.plugin.pdfutilities.features.AnnotationID;
import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.docear.plugin.pdfutilities.features.AnnotationNodeModel;
import org.docear.plugin.pdfutilities.features.IAnnotation;
import org.docear.plugin.pdfutilities.features.IAnnotation.AnnotationType;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.ui.conflict.ImportConflictDialog;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

import de.intarsys.pdf.cos.COSRuntimeException;
import de.intarsys.pdf.parser.COSLoadException;

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
				
				NodeUtils.insertNewChildNodesFrom(annotations, selected.isLeft(), selected, selected);
			} catch (COSRuntimeException e) {
				LogUtils.severe("ImportAllChildAnnotationsAction COSRuntimeException at URI("+uri+"): ", e); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (IOException e) {
				LogUtils.severe("ImportAllChildAnnotationsAction IOException at URI("+uri+"): ", e); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (COSLoadException e) {
				LogUtils.severe("ImportAllChildAnnotationsAction COSLoadException at URI("+uri+"): ", e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

}
