package org.docear.plugin.pdfutilities.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Map;

import org.docear.plugin.pdfutilities.features.AnnotationController;
import org.docear.plugin.pdfutilities.features.AnnotationID;
import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.docear.plugin.pdfutilities.features.AnnotationNodeModel;
import org.docear.plugin.pdfutilities.features.IAnnotation;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.ui.ImportConflictDialog;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.docear.plugin.pdfutilities.util.Tools;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.util.LogUtils;
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
			URI uri = Tools.getAbsoluteUri(selected);
            try {
            	PdfAnnotationImporter importer = new PdfAnnotationImporter();            	
				Collection<AnnotationModel> annotations = importer.importAnnotations(uri);
				NodeUtils nodeUtils = new NodeUtils();
				Map<AnnotationID, Collection<AnnotationNodeModel>> oldAnnotations = nodeUtils.getOldAnnotationsFromCurrentMap();
				annotations = AnnotationController.markNewAnnotations(annotations, oldAnnotations);
				Map<AnnotationID, Collection<IAnnotation>> conflicts = AnnotationController.getConflictedAnnotations(annotations, oldAnnotations);
				if(conflicts.size() > 0){
					ImportConflictDialog dialog = new ImportConflictDialog(Controller.getCurrentController().getViewController().getJFrame(), conflicts);
					dialog.showDialog();
				}
				System.out.println("Test");
                nodeUtils.insertNewChildNodesFrom(annotations, selected.isLeft(), selected);
			} catch (IOException e) {
				LogUtils.severe("ImportAllAnnotationsAction IOException at URI("+uri+"): ", e);
			} catch (COSLoadException e) {
				LogUtils.severe("ImportAllAnnotationsAction ImportException at URI("+uri+"): ", e);
			}	
		}

	}

}
