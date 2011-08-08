package org.docear.plugin.pdfutilities.actions;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotation;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.pdf.PdfFileFilter;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.docear.plugin.pdfutilities.util.Tools;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.clipboard.ClipboardController;
import org.freeplane.features.clipboard.mindmapmode.MClipboardController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.view.swing.features.filepreview.ViewerController;



public class DocearPasteAction extends AFreeplaneAction {

	public DocearPasteAction() {
		super("PasteAction");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unchecked")
	public void actionPerformed(final ActionEvent evt) {
		//TODO: Duplicate code with DocearNodeDropListener, needs to be refactored !!
		final MClipboardController clipboardController = (MClipboardController) ClipboardController.getController();
		final NodeModel parent = Controller.getCurrentController().getSelection().getSelected();
		final Transferable transferable = clipboardController.getClipboardContents();
		
		try{
			final DataFlavor fileListFlavor = new DataFlavor("application/x-java-file-list; class=java.util.List");
			final DataFlavor uriListFlavor = new DataFlavor("text/uri-list; class=java.lang.String");
			if(transferable.isDataFlavorSupported(fileListFlavor) || transferable.isDataFlavorSupported(uriListFlavor)){
				
				List<File> fileList = new ArrayList<File>();	            
	            PdfFileFilter pdfFileFilter = new PdfFileFilter();        
	           
	            
	            if(transferable.isDataFlavorSupported(fileListFlavor)){	            	
	                fileList = (List<File>) (transferable.getTransferData(fileListFlavor));
	            }
	            else if(transferable.isDataFlavorSupported(uriListFlavor)){	            	
	                fileList = Tools.textURIListToFileList((String) transferable.getTransferData(uriListFlavor));
	            }	            
	            
	            for(File file : fileList){	                
	            	boolean importAnnotations = ResourceController.getResourceController().getBooleanProperty(PdfUtilitiesController.AUTO_IMPORT_ANNOTATIONS_KEY);
	                if(pdfFileFilter.accept(file) && importAnnotations){
	                	PdfAnnotationImporter importer = new PdfAnnotationImporter();
	                    List<PdfAnnotation> annotations = importer.importAnnotations(file);
	                    NodeUtils nodeUtils = new NodeUtils();	                    
	                    nodeUtils.insertChildNodesFromPdf(file, annotations, parent.isNewChildLeft(), parent);
	                }
	                else{	                	
	        			ModeController modeController = Controller.getCurrentModeController();
	        			ViewerController viewerController = ((ViewerController)modeController.getExtension(ViewerController.class));
	        			if(!viewerController.paste(file, parent, parent.isNewChildLeft())){
	        				NodeUtils nodeUtils = new NodeUtils();
	        				nodeUtils.insertChildNodeFrom(file, parent.isNewChildLeft(), parent, null);
	        			}
	                }
	            }
				return;
			}
		} catch (Exception e) {
			clipboardController.paste(clipboardController.getClipboardContents(), parent, false, parent.isNewChildLeft());
		}
		clipboardController.paste(clipboardController.getClipboardContents(), parent, false, parent.isNewChildLeft());
	}

}
