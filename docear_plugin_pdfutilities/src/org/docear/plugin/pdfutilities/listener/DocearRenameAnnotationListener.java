package org.docear.plugin.pdfutilities.listener;

import java.io.IOException;

import javax.swing.JOptionPane;

import org.docear.plugin.core.features.AnnotationNodeModel;
import org.docear.plugin.core.features.IAnnotation.AnnotationType;
import org.docear.plugin.core.mindmap.AnnotationController;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

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
