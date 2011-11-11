package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;

public class AddNewGroupAction extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AddNewGroupAction() {
		super("workspace.action.node.group.new");
	}

	public void actionPerformed(final ActionEvent e) {
		System.out.println("workspace.action.node.group.new: " + e.getActionCommand() + " : " + e.getID());
		
		String groupName = JOptionPane.showInputDialog(Controller.getCurrentController().getViewController().getContentPane(),
				TextUtils.getText("enter_group_name"));
		
		WorkspaceUtils.createVirtualFolderNode(groupName, this.getNodeFromActionEvent(e));
		WorkspaceUtils.saveCurrentConfiguration();	
		
		WorkspaceController.getController().reloadWorkspace();
	}

}
