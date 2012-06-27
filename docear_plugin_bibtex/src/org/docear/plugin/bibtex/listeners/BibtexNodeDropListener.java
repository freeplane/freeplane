package org.docear.plugin.bibtex.listeners;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;

import javax.swing.SwingUtilities;


import net.sf.jabref.BibtexEntry;
import net.sf.jabref.groups.TransferableEntrySelection;

import org.docear.plugin.bibtex.ReferenceUpdater;
import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.bibtex.jabref.JabRefAttributes;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.docear.plugin.pdfutilities.listener.DocearNodeDropListener;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
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
		
		try{
						
			if (dtde.isLocalTransfer() && dtde.isDataFlavorSupported(TransferableEntrySelection.flavorInternal)) {
				mainView.setDraggedOver(NodeView.DRAGGED_OVER_NO);
	            mainView.repaint();
	            final Transferable transferable = dtde.getTransferable();
	            
	            dtde.acceptDrop(dtde.getDropAction());
	            TransferableEntrySelection selection = (TransferableEntrySelection)transferable.getTransferData(TransferableEntrySelection.flavorInternal);
	            for(BibtexEntry entry : selection.selectedEntries){
	            	JabRefAttributes jabRefAttributes = ReferencesController.getController().getJabRefAttributes();
	            	jabRefAttributes.setReferenceToNode(entry, targetNode);
	            	if (jabRefAttributes.isNodeDirty()) {
	            		jabRefAttributes.setNodeDirty(false);
	            		SwingUtilities.invokeLater(new Runnable() {					
							@Override
							public void run() {								
								MindmapUpdateController mindmapUpdateController = new MindmapUpdateController(false);
								mindmapUpdateController.addMindmapUpdater(new ReferenceUpdater(TextUtils.getText("update_references_open_mindmaps")));
								mindmapUpdateController.updateCurrentMindmap(true);
							}
						});
	            	}	            	
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
