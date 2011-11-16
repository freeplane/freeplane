package org.freeplane.plugin.workspace.config.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import org.freeplane.core.ui.EnabledAction;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;

@EnabledAction(checkOnNodeChange = true, checkOnPopup = true)
public class NodePasteAction extends AWorkspaceAction {

	private static final long serialVersionUID = 1L;

	public NodePasteAction() {
		super("workspace.action.node.paste");
	}
	
	public void setEnabled() { 
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
		//FIXME: this function is not available yet
		setEnabled(false);
	}
	
	public void actionPerformed(final ActionEvent e) {
        //TODO: IMPLEMENTATION
    }


}
