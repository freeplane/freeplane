package org.docear.plugin.pdfutilities;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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
	                fileList = this.textURIListToFileList((String) transferable.getTransferData(uriListFlavor));
	            }	            
	            
	            for(File file : fileList){
	                //TODO: ask for auto import Action
	            	boolean importAnnotations = true; //ResourceController.getResourceController().getBooleanProperty("docear_import_annotations");
	                if(pdfFileFilter.accept(file) && importAnnotations){
	                	PdfAnnotationImporter importer = new PdfAnnotationImporter();
	                    List<PdfAnnotation> annotations = importer.importAnnotations(file);
	                    NodeUtils nodeUtils = new NodeUtils();
	                    final boolean isLeft = mainView.dropLeft(dtde.getLocation().getX());
	                    nodeUtils.insertChildNodesFrom(file, annotations, isLeft, targetNode);
	                }
	                else{
	                	final boolean isLeft = mainView.dropLeft(dtde.getLocation().getX());
	        			ModeController modeController = controller.getModeController();
	        			ViewerController viewerController = ((ViewerController)modeController.getExtension(ViewerController.class));
	        			if(!viewerController.paste(file, targetNode, isLeft)){
	        				NodeUtils nodeUtils = new NodeUtils();
	        				nodeUtils.insertChildNodeFrom(file, isLeft, targetNode);
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
	
	private List<File> textURIListToFileList(String data) {
	    List<File> list = new ArrayList<File>();
	    StringTokenizer stringTokenizer = new StringTokenizer(data, "\r\n");
	    while(stringTokenizer.hasMoreTokens()) {
	    	String string = stringTokenizer.nextToken();
	    	// the line is a comment (as per the RFC 2483)
	    	if (string.startsWith("#")) continue;
		    		    
			try {
				URI uri = new URI(string);
				File file = new File(uri);
			    list.add(file);
			} catch (URISyntaxException e) {
				LogUtils.warn("DocearNodeDropListener could not parse uri to file because an URISyntaxException occured. URI: " + string);
			} catch (IllegalArgumentException e) {
				LogUtils.warn("DocearNodeDropListener could not parse uri to file because an IllegalArgumentException occured. URI: " + string);
		    }	    
	    }	     
	    return list;
	}	

}
