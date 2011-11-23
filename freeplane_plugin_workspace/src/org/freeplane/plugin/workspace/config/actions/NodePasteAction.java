package org.freeplane.plugin.workspace.config.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.dnd.IDropAcceptor;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.model.action.AWorkspaceAction;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

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
		//FIXME: this function is not available yet
		setEnabled(false);
	}
	
	public void setEnabledFor(AWorkspaceTreeNode node) {
		if(!(node instanceof IDropAcceptor)) {
			setEnabled(false);
			return;
		}
		super.setEnabledFor(node);
	}
	
	public void actionPerformed(final ActionEvent e) {
        //TODO: IMPLEMENTATION
    }


}
