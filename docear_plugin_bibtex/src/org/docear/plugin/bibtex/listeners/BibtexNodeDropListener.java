package org.docear.plugin.bibtex.listeners;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.List;

import net.sf.jabref.BibtexEntry;
import net.sf.jabref.groups.TransferableEntrySelection;

import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.pdfutilities.listener.DocearNodeDropListener;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.NodeView;

public class BibtexNodeDropListener extends DocearNodeDropListener {
	
	public BibtexNodeDropListener(){
		super();
	}
	
	public void drop(final DropTargetDropEvent dtde) {
		LogUtils.info("BibtexNodeDropListener Drop activated....");
		final MainView mainView = (MainView) dtde.getDropTargetContext().getComponent();
		final NodeView targetNodeView = mainView.getNodeView();
		final NodeModel targetNode = targetNodeView.getModel();
		//final Controller controller = Controller.getCurrentController();
		
		try{
						
			if (dtde.isLocalTransfer() && dtde.isDataFlavorSupported(TransferableEntrySelection.flavorInternal)) {
				mainView.setDraggedOver(NodeView.DRAGGED_OVER_NO);
	            mainView.repaint();
	            final Transferable transferable = dtde.getTransferable();
	            
	            dtde.acceptDrop(dtde.getDropAction());
	            TransferableEntrySelection selection = (TransferableEntrySelection)transferable.getTransferData(TransferableEntrySelection.flavorInternal);
	            for(BibtexEntry entry : selection.selectedEntries){
	            	ReferencesController.getController().getJabRefAttributes().setReferenceToNode(entry, targetNode);
	            	break;
	            }
	            dtde.dropComplete(true);
				return;
			}
			
		} catch (final Exception e) {
			LogUtils.severe("BibtexNodeDropListener Drop exception:", e);
			dtde.dropComplete(false);
			return;
		}
		super.drop(dtde);
	}
	
	
	public boolean isDragAcceptable(final DropTargetDragEvent ev) {
		
		if(ev.isDataFlavorSupported(TransferableEntrySelection.flavorInternal)){
			return true;
		}
		return super.isDragAcceptable(ev);		
	}

}
