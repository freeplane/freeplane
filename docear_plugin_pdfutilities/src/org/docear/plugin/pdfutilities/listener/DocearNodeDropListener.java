package org.docear.plugin.pdfutilities.listener;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.SwingUtilities;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.ui.SwingWorkerDialog;
import org.docear.plugin.core.util.Tools;
import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.pdf.PdfFileFilter;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.clipboard.MindMapNodesSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.view.swing.features.filepreview.ViewerController;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.ui.mindmapmode.MNodeDropListener;
import org.jdesktop.swingworker.SwingWorker;

import de.intarsys.pdf.cos.COSRuntimeException;
import de.intarsys.pdf.parser.COSLoadException;


public class DocearNodeDropListener extends MNodeDropListener {
	
	public DocearNodeDropListener(){
		super();
	}	
	
	@SuppressWarnings("unchecked")
	public void drop(final DropTargetDropEvent dtde) {
		LogUtils.info("DocearNodedroplistener Drop activated...."); //$NON-NLS-1$
					
		final MainView mainView = (MainView) dtde.getDropTargetContext().getComponent();
		final NodeView targetNodeView = mainView.getNodeView();
		final NodeModel targetNode = targetNodeView.getModel();		
		
		try{
			final DataFlavor fileListFlavor = new DataFlavor("application/x-java-file-list; class=java.util.List"); //$NON-NLS-1$
			final DataFlavor uriListFlavor = new DataFlavor("text/uri-list; class=java.lang.String"); //$NON-NLS-1$
			//TODO: DOCEAR - why restrict to !dtde.isLocalTransfer only?
			if(dtde.isDataFlavorSupported(MindMapNodesSelection.mindMapNodesFlavor) ) {
				super.drop(dtde);
				return;
			}
			// do not combine with the previous condition unless you know what you are doing! 
			if (dtde.isDataFlavorSupported(fileListFlavor) || (dtde.isDataFlavorSupported(uriListFlavor))) {
	            				
	            
	            final Transferable transferable = dtde.getTransferable();
	            final boolean isLeft = mainView.dropLeft(dtde.getLocation().getX());
	            mainView.setDraggedOver(NodeView.DRAGGED_OVER_NO);
	            mainView.repaint();
	            
	            List<File> fileList = new ArrayList<File>();
	            if(transferable.isDataFlavorSupported(fileListFlavor)){
	    			dtde.acceptDrop(dtde.getDropAction());
	    		    fileList = (List<File>) (transferable.getTransferData(fileListFlavor));
	    		}
	    		else if(transferable.isDataFlavorSupported(uriListFlavor)){
	    			dtde.acceptDrop(dtde.getDropAction());
	    		    fileList = Tools.textURIListToFileList((String) transferable.getTransferData(uriListFlavor));
	    		}	
	            
	            pasteFileList(fileList, targetNode, isLeft);
	            
	            dtde.dropComplete(true);
	            return;		
	        }
		 } catch (final Exception e) {
			LogUtils.severe("DocearNodeDropListener Drop exception:", e); //$NON-NLS-1$
			dtde.dropComplete(false);
			return;
		 }
		 super.drop(dtde);
	}

	public static void pasteFileList(final List<File> fileList, final NodeModel targetNode, final boolean isLeft)
			throws UnsupportedFlavorException, IOException, ClassNotFoundException, Exception {	
		
		
		SwingWorker<Void, Void> thread = new SwingWorker<Void, Void>(){

			@Override
			protected Void doInBackground() throws Exception {
				int count = 0;
				firePropertyChange(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
				for(final File file : fileList){	
					if(Thread.currentThread().isInterrupted()) return null;
					firePropertyChange(SwingWorkerDialog.NEW_FILE, null, file.getName());
		        	boolean importAnnotations = ResourceController.getResourceController().getBooleanProperty(PdfUtilitiesController.AUTO_IMPORT_ANNOTATIONS_KEY);
		            if(new PdfFileFilter().accept(file) && importAnnotations){
		            	try{
		            		PdfAnnotationImporter importer = new PdfAnnotationImporter();
		            		final List<AnnotationModel> annotations = importer.importAnnotations(file.toURI());	            		
		            		SwingUtilities.invokeAndWait(
							        new Runnable() {
							            public void run(){
							            	try {
								            	URI uri = file.toURI();
								            	NodeModel newNode = NodeUtils.insertChildNodesFromPdf(uri, annotations, isLeft, targetNode);	            
								            	for(AnnotationModel annotation : getInsertedNodes(annotations)){
													firePropertyChange(SwingWorkerDialog.DETAILS_LOG_TEXT, null, TextUtils.getText("DocearNodeDropListener.4") + annotation.getTitle() +TextUtils.getText("DocearNodeDropListener.5"));												 //$NON-NLS-1$ //$NON-NLS-2$
												}	
								            	DocearEvent event = new DocearEvent(newNode, DocearEventType.MINDMAP_ADD_PDF_TO_NODE, true);
								            	DocearController.getController().dispatchDocearEvent(event);
							            	}
							            	catch (Exception e) {
							            		LogUtils.severe(e);
							            	}
							            }
							        }
							   );						
		            		    	
		            	} catch(COSRuntimeException e) {			                		
		            		LogUtils.warn("Exception during import on file: " + file.getName(), e); //$NON-NLS-1$
		            	} catch(IOException e) {
		            		LogUtils.warn("Exception during import on file: " + file.getName(), e); //$NON-NLS-1$
		            	} catch(COSLoadException e) {
		            		LogUtils.warn("Exception during import on file: " + file.getName(), e); //$NON-NLS-1$
		            	}
		            }
		            else {		            	
		    			ModeController modeController = Controller.getCurrentController().getModeController();
		    			final ViewerController viewerController = ((ViewerController)modeController.getExtension(ViewerController.class));
		    			SwingUtilities.invokeAndWait(
						        new Runnable() {
						            public void run(){
						            	if(!viewerController.paste(file, targetNode, isLeft)){							        				
					        				NodeUtils.insertChildNodeFrom(file.toURI(), isLeft, targetNode, null);
					        			}							
						            }
						        }
						   );		        			
		            }
		            count++;
					setProgress(100 * count / fileList.size());
					Thread.sleep(1L);
		        }
				return null;
			}
			
			@Override
		    protected void done() {
				firePropertyChange(SwingWorkerDialog.IS_DONE, null, null);
			}
			
			private Collection<AnnotationModel> getInsertedNodes(Collection<AnnotationModel> annotations){
				Collection<AnnotationModel> result = new ArrayList<AnnotationModel>();
				for(AnnotationModel annotation : annotations){
					result.add(annotation);
					result.addAll(this.getInsertedNodes(annotation.getChildren()));							
				}
				return result;
			}
			
		};
		
		/*if(fileList.size() > 10){
			SwingWorkerDialog monitoringDialog = new SwingWorkerDialog(Controller.getCurrentController().getViewController().getJFrame());
			monitoringDialog.showDialog(thread);
		}
		else{*/
			thread.execute();
		//}
	}
	
	/*public boolean isDragAcceptable(final DropTargetDragEvent ev) {
		if(ev.isDataFlavorSupported(TransferableEntrySelection.flavorInternal)){
			return true;
		}
		return super.isDragAcceptable(ev);
		
	}*/
	

}
