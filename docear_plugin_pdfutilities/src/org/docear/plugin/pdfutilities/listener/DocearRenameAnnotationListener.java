package org.docear.plugin.pdfutilities.listener;

import java.io.IOException;

import javax.swing.JOptionPane;

import org.docear.plugin.pdfutilities.features.AnnotationController;
import org.docear.plugin.pdfutilities.features.AnnotationNodeModel;
import org.docear.plugin.pdfutilities.features.IAnnotation.AnnotationType;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.NodeModel;

import de.intarsys.pdf.cos.COSRuntimeException;
import de.intarsys.pdf.parser.COSLoadException;

public class DocearRenameAnnotationListener implements INodeChangeListener {

	public void nodeChanged(org.freeplane.features.map.NodeChangeEvent event) {
		
		if(event.getProperty().equals(NodeModel.NODE_TEXT)){
			NodeModel node = event.getNode();
			AnnotationNodeModel annotation = AnnotationController.getAnnotationNodeModel(node);
			if(annotation != null && annotation.getAnnotationType() != null && !annotation.getAnnotationType().equals(AnnotationType.PDF_FILE)){
				try {
					new PdfAnnotationImporter().renameAnnotation(annotation, event.getNewValue().toString());
				} catch (IOException e) {
					if(e.getMessage().equals("destination is read only")){
						int result = UITools.showConfirmDialog(node, "Could not rename annotation in PDF file.\n Please close the file in other Applications. ", "Could not rename annotation.", JOptionPane.OK_CANCEL_OPTION);
						if( result == JOptionPane.OK_OPTION){
							this.nodeChanged(event);
						}
						else{
							//Controller.getCurrentModeController().rollback();
						}
					}
					else{
						LogUtils.severe("DocearRenameAnnotationListener IOException at Target("+node.getText()+"): ", e);
					}
				} catch (COSLoadException e) {
					LogUtils.severe("DocearRenameAnnotationListener COSLoadException at Target("+node.getText()+"): ", e);
				} catch (COSRuntimeException e) {
					LogUtils.severe("DocearRenameAnnotationListener COSRuntimeException at Target("+node.getText()+"): ", e);
				}
			}
		}
		
		
	}

}
