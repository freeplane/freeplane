package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import org.freeplane.plugin.workspace.WorkspaceController;

public class WorkspaceRefreshAction extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WorkspaceRefreshAction() {
		super("workspace.action.node.refresh");
	}

	public void actionPerformed(final ActionEvent e) {		
		WorkspaceController.getController().reloadWorkspace();	
	}	
	
	
}
