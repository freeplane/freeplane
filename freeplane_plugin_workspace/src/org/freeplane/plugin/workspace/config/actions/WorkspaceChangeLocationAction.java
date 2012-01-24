package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
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
        WorkspaceUtils.saveCurrentConfiguration();
		showLocationSwitcherDialog();		
	}
	
	private void showLocationSwitcherDialog() {
		closeAllMindMaps();	
		WorkspaceUtils.showWorkspaceChooserDialog();
		
		WorkspaceController workspaceController = WorkspaceController.getController();
		workspaceController.reloadWorkspace();
		

	}
	
	private void closeAllMindMaps() {
		while(Controller.getCurrentController().getMap() != null) {
			Controller.getCurrentController().close(false);
		}	
	}

}
