package org.docear.plugin.pdfutilities.listener;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.pdf.PdfFileFilter;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.docear.plugin.pdfutilities.util.Tools;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.view.swing.features.filepreview.ViewerController;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.ui.mindmapmode.MNodeDropListener;


public class DocearNodeDropListener extends MNodeDropListener {
	
	public DocearNodeDropListener(){
		super();
	}	
	
	@SuppressWarnings("unchecked")
	public void drop(final DropTargetDropEvent dtde) {
		LogUtils.info("DocearNodedroplistener Drop activated....");
					
		final MainView mainView = (MainView) dtde.getDropTargetContext().getComponent();
		final NodeView targetNodeView = mainView.getNodeView();
		final NodeModel targetNode = targetNodeView.getModel();
		final Controller controller = Controller.getCurrentController();
		
		try{
			final DataFlavor fileListFlavor = new DataFlavor("application/x-java-file-list; class=java.util.List");
			final DataFlavor uriListFlavor = new DataFlavor("text/uri-list; class=java.lang.String");
			if (!dtde.isLocalTransfer() || dtde.isDataFlavorSupported(fileListFlavor)) {
	            				
	            List<File> fileList = new ArrayList<File>();
	            final Transferable transferable = dtde.getTransferable();
	            PdfFileFilter pdfFileFilter = new PdfFileFilter();
	            
	            mainView.setDraggedOver(NodeView.DRAGGED_OVER_NO);
	            mainView.repaint();
	            
	            if(transferable.isDataFlavorSupported(fileListFlavor)){
	            	dtde.acceptDrop(dtde.getDropAction());
	                fileList = (List<File>) (transferable.getTransferData(fileListFlavor));
	            }
	            else if(transferable.isDataFlavorSupported(uriListFlavor)){
	            	dtde.acceptDrop(dtde.getDropAction());
	                fileList = Tools.textURIListToFileList((String) transferable.getTransferData(uriListFlavor));
	            }	            
	            
	            for(File file : fileList){	                
	            	boolean importAnnotations = ResourceController.getResourceController().getBooleanProperty(PdfUtilitiesController.AUTO_IMPORT_ANNOTATIONS_KEY);
	                if(pdfFileFilter.accept(file) && importAnnotations){
	                	PdfAnnotationImporter importer = new PdfAnnotationImporter();
	                    List<AnnotationModel> annotations = importer.importAnnotations(file.toURI());
	                    NodeUtils nodeUtils = new NodeUtils();
	                    final boolean isLeft = mainView.dropLeft(dtde.getLocation().getX());
	                    nodeUtils.insertChildNodesFromPdf(file.toURI(), annotations, isLeft, targetNode);
	                }
	                else{
	                	final boolean isLeft = mainView.dropLeft(dtde.getLocation().getX());
	        			ModeController modeController = controller.getModeController();
	        			ViewerController viewerController = ((ViewerController)modeController.getExtension(ViewerController.class));
	        			if(!viewerController.paste(file, targetNode, isLeft)){
	        				NodeUtils nodeUtils = new NodeUtils();
	        				nodeUtils.insertChildNodeFrom(file.toURI(), isLeft, targetNode, null);
	        			}
	                }
	            }
	            
	            dtde.dropComplete(true);
	            return;		
	        }
		 } catch (final Exception e) {
			LogUtils.severe("DocearNodeDropListener Drop exception:", e);
			dtde.dropComplete(false);
			return;
		 }
		 super.drop(dtde);
	}
	

}
