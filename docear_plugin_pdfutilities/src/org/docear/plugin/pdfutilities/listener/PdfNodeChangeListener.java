package org.docear.plugin.pdfutilities.listener;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.swing.JOptionPane;

import org.docear.pdf.PdfDataExtractor;
import org.docear.plugin.core.features.AnnotationID;
import org.docear.plugin.core.util.Tools;
import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.docear.plugin.pdfutilities.features.AnnotationNodeModel;
import org.docear.plugin.pdfutilities.features.IAnnotation.AnnotationType;
import org.docear.plugin.pdfutilities.map.AnnotationController;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.pdf.PdfFileFilter;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

import de.intarsys.pdf.cos.COSRuntimeException;
import de.intarsys.pdf.parser.COSLoadException;

public class PdfNodeChangeListener implements INodeChangeListener {

	public void nodeChanged(org.freeplane.features.map.NodeChangeEvent event) {		
		if (event.getProperty().equals(NodeModel.HYPERLINK_CHANGED)) {
			URI newUri = (URI) event.getNewValue();
			if (newUri != null) {
				try{
					//DOCEAR -this seems not right -> what if the uri is relative to the map?
					File file = Tools.getFilefromUri(Tools.getAbsoluteUri(newUri));
					if(new PdfFileFilter().accept(file)) {
						AnnotationModel model = AnnotationController.getModel(event.getNode(), false);
						if(model == null){
							model = new AnnotationModel();
							model.setAnnotationID(new AnnotationID(newUri, 0));
							model.setAnnotationType(AnnotationType.PDF_FILE);							
							AnnotationController.setModel(event.getNode(), model);
						}
						try {
							PdfDataExtractor extractor = new PdfDataExtractor(Tools.getAbsoluteUri(event.getNode()));
							String hash = extractor.getUniqueHashCode();
							model.setDocumentHash(hash);
						}
						catch (Exception e) {
							LogUtils.info("could not get unique file hash: "+ e.getMessage());
						}
					}
				}
				catch(Exception e){
					LogUtils.warn(e);
				}
			}
			else if(AnnotationController.getModel(event.getNode(), false) != null){
				AnnotationController.setModel(event.getNode(), null);
			}
		}
		else if(event.getProperty().equals(NodeModel.NODE_TEXT)){
			NodeModel node = event.getNode();
			AnnotationNodeModel annotation = AnnotationController.getAnnotationNodeModel(node);
			if(annotation != null && annotation.getAnnotationType() != null && !annotation.getAnnotationType().equals(AnnotationType.PDF_FILE)){
				try {
					new PdfAnnotationImporter().renameAnnotation(annotation, event.getNewValue().toString());
					System.gc();
				} catch (IOException e) {
					if(e.getMessage().equals("destination is read only")){ //$NON-NLS-1$
						Object[] options = { TextUtils.getText("DocearRenameAnnotationListener.1"), TextUtils.getText("DocearRenameAnnotationListener.2"),TextUtils.getText("DocearRenameAnnotationListener.3") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						int result = JOptionPane.showOptionDialog(Controller.getCurrentController().getViewController().getComponent(node), TextUtils.getText("DocearRenameAnnotationListener.4"), TextUtils.getText("DocearRenameAnnotationListener.5"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]); //$NON-NLS-1$ //$NON-NLS-2$
						if( result == JOptionPane.OK_OPTION){
							this.nodeChanged(event);
						}
						else if( result == JOptionPane.CANCEL_OPTION ){
							//Controller.getCurrentModeController().rollback();
							node.setText("" + event.getOldValue());							 //$NON-NLS-1$
						}
					}
					else{
						LogUtils.severe("DocearRenameAnnotationListener IOException at Target("+node.getText()+"): ", e); //$NON-NLS-1$ //$NON-NLS-2$
					}
				} catch (COSLoadException e) {
					LogUtils.severe("DocearRenameAnnotationListener COSLoadException at Target("+node.getText()+"): ", e); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (COSRuntimeException e) {
					LogUtils.severe("DocearRenameAnnotationListener COSRuntimeException at Target("+node.getText()+"): ", e); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}		
	}

}
