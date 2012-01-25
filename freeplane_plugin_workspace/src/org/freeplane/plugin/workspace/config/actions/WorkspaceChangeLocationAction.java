package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

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
        WorkspaceUtils.saveCurrentConfiguration();
		showLocationSwitcherDialog();		
	}
	
	private void showLocationSwitcherDialog() {
		WorkspaceUtils.showWorkspaceChooserDialog();
	}
	
	

}
