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
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;

@CheckEnableOnPopup
public class NodeCutAction extends AWorkspaceAction {

	public static final String KEY = "workspace.action.node.cut";
	private static final long serialVersionUID = 1L;

	public NodeCutAction() {
		super(KEY);
	}
		
	public void setEnabledFor(AWorkspaceTreeNode node, TreePath[] selectedPaths) {
		if(node.isSystem() || !node.isTransferable() || !(node instanceof IWorkspaceTransferableCreator) || (node instanceof DefaultFileNode)) {
			setEnabled(false);
		}
		else{
			setEnabled();
		}
	}
	
	public void actionPerformed(final ActionEvent event) {
		AWorkspaceTreeNode[] targetNodes = getSelectedNodes(event);
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
		transferable.setAsCopy(false);   
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, new CutClipboardOwner(transferable));
			
//		AWorkspaceTreeNode parent = targetNode.getParent();
//		
//		targetNode.getModel().cutNodeFromParent(targetNode);
//		if(parent != null) {
//			parent.refresh();
//			//parent.getModel().requestSave();
//		}
		
    }
	
	class CutClipboardOwner implements IWorspaceClipboardOwner {
		private final WorkspaceTransferable transferable;

		public CutClipboardOwner(WorkspaceTransferable transfer) {
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
