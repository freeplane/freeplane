package org.freeplane.plugin.workspace.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;

import javax.swing.tree.TreePath;

import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;
import org.freeplane.plugin.workspace.dnd.DnDController;
import org.freeplane.plugin.workspace.dnd.NoDropHandlerFoundExeption;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

@CheckEnableOnPopup
public class NodePasteAction extends AWorkspaceAction {

	public static final String KEY = "workspace.action.node.paste";
	private static final long serialVersionUID = 1L;

	public NodePasteAction() {
		super(KEY);
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
	
	public void setEnabledFor(AWorkspaceTreeNode node, TreePath[] selectedPaths) {
		if(!DnDController.isDropAllowed(node)) {
			setEnabled(false);
			return;
		}
		super.setEnabledFor(node, selectedPaths);
	}
	
	public void actionPerformed(final ActionEvent e) {
        AWorkspaceTreeNode targetNode = getNodeFromActionEvent(e);
        if(DnDController.isDropAllowed(targetNode)) {
        	Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        	Transferable transf = clip.getContents(null);
        	if(transf == null) {
        		return;
        	}
        	int dndAction = DnDConstants.ACTION_COPY;
        	if(transf.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_MOVE_NODE_FLAVOR)) {
        		dndAction = DnDConstants.ACTION_MOVE;
        	}
        	if(WorkspaceController.getCurrentModeExtension().getView() != null) {
        		try {
					WorkspaceController.getCurrentModeExtension().getView().getTransferHandler().handleDrop(targetNode, transf, dndAction);
					if(dndAction == DnDConstants.ACTION_MOVE ) {
						clip.setContents(null, null);
					}
				} catch (NoDropHandlerFoundExeption ex) {
					LogUtils.info("Exception in org.freeplane.plugin.workspace.actions.NodePasteAction.actionPerformed(ActionEvent): "+ ex.getMessage());
				}
        	}
        }
    }


}
