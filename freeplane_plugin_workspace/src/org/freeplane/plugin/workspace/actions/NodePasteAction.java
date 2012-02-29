package org.freeplane.plugin.workspace.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;

import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.dnd.IDropAcceptor;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

@EnabledAction(checkOnPopup = true)
public class NodePasteAction extends AWorkspaceAction {

	private static final long serialVersionUID = 1L;

	public NodePasteAction() {
		super("workspace.action.node.paste");
	}
	
	public void setEnabled() {
		try {
			if(Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR) 
					|| Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(WorkspaceTransferable.WORKSPACE_URI_LIST_FLAVOR)
					|| Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(WorkspaceTransferable.WORKSPACE_NODE_FLAVOR)
					|| Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(WorkspaceTransferable.WORKSPACE_FREEPLANE_NODE_FLAVOR)
			) {
				setEnabled(true);			
			} 
			else {
				setEnabled(false);
			}
		} 
		catch (Exception ex) {
			// if the system clipboard has a problem
			LogUtils.warn(ex.getLocalizedMessage());
		}
	}
	
	public void setEnabledFor(AWorkspaceTreeNode node) {
		if(!(node instanceof IDropAcceptor)) {
			setEnabled(false);
			return;
		}
		super.setEnabledFor(node);
	}
	
	public void actionPerformed(final ActionEvent e) {
        AWorkspaceTreeNode targetNode = getNodeFromActionEvent(e);
        if(targetNode instanceof IDropAcceptor) {
        	Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        	Transferable transf = clip.getContents(null);
        	if(transf == null) {
        		return;
        	}
        	((IDropAcceptor) targetNode).processDrop(transf, DnDConstants.ACTION_COPY);
        }
    }


}
