package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import org.freeplane.plugin.workspace.WorkspaceChooserDialog;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.action.AWorkspaceAction;

public class WorkspaceChangeLocationAction extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WorkspaceChangeLocationAction() {
		super("workspace.action.location.change");
	}

	public void actionPerformed(final ActionEvent e) {
		System.out.println("WorkspaceSetLocationAction: " + e.getActionCommand() + " : " + e.getID());
		showLocationSwitcherDialog();		
	}
	
	private void showLocationSwitcherDialog() {		
		WorkspaceChooserDialog locationDialog = new WorkspaceChooserDialog();
		locationDialog.setVisible(true);
		
		WorkspaceController workspaceController = WorkspaceController.getController();
		workspaceController.reloadWorkspace();
		

	}

}
