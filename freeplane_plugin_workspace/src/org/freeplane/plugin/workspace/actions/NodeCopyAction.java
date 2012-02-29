package org.freeplane.plugin.workspace.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

import org.freeplane.core.ui.EnabledAction;
import org.freeplane.plugin.workspace.dnd.IWorkspaceTransferableCreator;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

@EnabledAction(checkOnPopup = true)
public class NodeCopyAction extends AWorkspaceAction {

	private static final long serialVersionUID = 1L;

	public NodeCopyAction() {
		super("workspace.action.node.copy");
	}
	
	public void setEnabledFor(AWorkspaceTreeNode node) {
		if(node.isSystem() || !node.isTransferable() || !(node instanceof IWorkspaceTransferableCreator)) {
			setEnabled(false);
		}
		else{
			setEnabled();
		}
	}
	
	public void actionPerformed(final ActionEvent e) {
       AWorkspaceTreeNode targetNode = getNodeFromActionEvent(e);
       if(targetNode instanceof IWorkspaceTransferableCreator) {
    	   Transferable transferable = ((IWorkspaceTransferableCreator) targetNode).getTransferable();
    	   Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
       }
    }


}
