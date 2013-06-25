package org.freeplane.plugin.workspace.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

import javax.swing.tree.TreePath;

import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;
import org.freeplane.plugin.workspace.dnd.DnDController;
import org.freeplane.plugin.workspace.dnd.IWorkspaceTransferableCreator;
import org.freeplane.plugin.workspace.dnd.IWorspaceClipboardOwner;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

@CheckEnableOnPopup
public class NodeCopyAction extends AWorkspaceAction {

	public static final String KEY = "workspace.action.node.copy";
	private static final long serialVersionUID = 1L;

	public NodeCopyAction() {
		super(KEY);
	}
	
	public void setEnabledFor(AWorkspaceTreeNode node, TreePath[] selectedPaths) {
		if(node.isSystem() || !node.isTransferable() || !(node instanceof IWorkspaceTransferableCreator)) {
			setEnabled(false);
		}
		else{
			setEnabled();
		}
	}
	
	public void actionPerformed(final ActionEvent e) {
		AWorkspaceTreeNode[] targetNodes = getSelectedNodes(e);
		WorkspaceTransferable transferable = null;
		for (AWorkspaceTreeNode targetNode : targetNodes) {
			if(targetNode instanceof IWorkspaceTransferableCreator) {
				if(transferable == null) {
					transferable = ((IWorkspaceTransferableCreator)targetNode).getTransferable();
				}
				else {
					transferable.merge(((IWorkspaceTransferableCreator)targetNode).getTransferable());
				}
			}
		}
		
       if(transferable == null) {
    	   return;
       }
       
       Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, new CopyClipboardOwner(transferable));
       
    }


	class CopyClipboardOwner implements IWorspaceClipboardOwner {
		private final WorkspaceTransferable transferable;

		public CopyClipboardOwner(WorkspaceTransferable transfer) {
			this.transferable = transfer;
			DnDController.getSystemClipboardController().setClipboardOwner(this);
		}
		
		public void lostOwnership(Clipboard clipboard, Transferable contents) {
			if(this.equals(DnDController.getSystemClipboardController().getClipboardOwner()) ) {
				DnDController.getSystemClipboardController().resetClipboardOwner(this);
				if(transferable != null) {
					transferable.refreshNodes();
				}
			}
			
		}

		public WorkspaceTransferable getTransferable() {
			return this.transferable;
		}
	}
}
