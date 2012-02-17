/**
 * author: Marcel Genzmehr
 * 11.11.2011
 */
package org.freeplane.plugin.workspace.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.components.dialog.WorkspaceNewFolderPanel;
import org.freeplane.plugin.workspace.components.dialog.WorkspaceNewGroupDialog;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;


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
//		WorkspaceNewFolderPanel panel = new WorkspaceNewFolderPanel(WorkspaceNewFolderPanel.MODE_PHYSICAL_ONLY, targetNode);
//		int response = JOptionPane.showConfirmDialog(UITools.getFrame(), panel, "Create new Folder...", JOptionPane.OK_CANCEL_OPTION);
		
	}
}
