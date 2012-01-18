package org.docear.plugin.pdfutilities.actions;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.docear.plugin.core.util.Tools;
import org.docear.plugin.pdfutilities.listener.DocearNodeDropListener;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.clipboard.ClipboardController;
import org.freeplane.features.clipboard.MindMapNodesSelection;
import org.freeplane.features.clipboard.mindmapmode.MClipboardController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;



public class DocearPasteAction extends AFreeplaneAction {

	public DocearPasteAction() {
		super("PasteAction"); //$NON-NLS-1$
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unchecked")
	public void actionPerformed(final ActionEvent evt) {		
		final MClipboardController clipboardController = (MClipboardController) ClipboardController.getController();
		final NodeModel targetNode = Controller.getCurrentController().getSelection().getSelected();
		final Transferable transferable = clipboardController.getClipboardContents();
		
		try{
			final DataFlavor fileListFlavor = new DataFlavor("application/x-java-file-list; class=java.util.List"); //$NON-NLS-1$
			final DataFlavor uriListFlavor = new DataFlavor("text/uri-list; class=java.lang.String"); //$NON-NLS-1$
			if(!transferable.isDataFlavorSupported(MindMapNodesSelection.mindMapNodesFlavor) && (transferable.isDataFlavorSupported(fileListFlavor) || transferable.isDataFlavorSupported(uriListFlavor))){
				
				List<File> fileList = new ArrayList<File>();           
	            
	            if(transferable.isDataFlavorSupported(fileListFlavor)){	            	
	                fileList = (List<File>) (transferable.getTransferData(fileListFlavor));
	            }
	            else if(transferable.isDataFlavorSupported(uriListFlavor)){	            	
	                fileList = Tools.textURIListToFileList((String) transferable.getTransferData(uriListFlavor));
	            }	            
	            DocearNodeDropListener.pasteFileList(fileList, targetNode, targetNode.isNewChildLeft());
	            
				return;
			}
		} catch (Exception e) {
			clipboardController.paste(clipboardController.getClipboardContents(), targetNode, false, targetNode.isNewChildLeft());
		}
		clipboardController.paste(clipboardController.getClipboardContents(), targetNode, false, targetNode.isNewChildLeft());
	}

}
