/**
 * author: Marcel Genzmehr
 * 11.11.2011
 */
package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.dialog.WorkspaceNewGroupDialog;
import org.freeplane.plugin.workspace.model.action.AWorkspaceAction;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;


public class NodeNewFolderAction extends AWorkspaceAction {

	private static final long serialVersionUID = 6126361617680877866L;

	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public NodeNewFolderAction() {
		super("workspace.action.node.new.folder");
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void actionPerformed(ActionEvent e) {
		AWorkspaceTreeNode targetNode = getNodeFromActionEvent(e);
		if(targetNode == null) {
			targetNode = (AWorkspaceTreeNode) WorkspaceUtils.getModel().getRoot();
		}
		WorkspaceNewGroupDialog dlg = new WorkspaceNewGroupDialog("Create new Folder...", targetNode);
		dlg.setVisible(true);
	}
}
