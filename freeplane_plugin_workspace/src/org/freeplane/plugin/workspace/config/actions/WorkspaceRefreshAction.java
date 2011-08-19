package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.plugin.workspace.WorkspaceController;

public class WorkspaceRefreshAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WorkspaceRefreshAction() {
		super("WorkspaceRefreshAction");
	}

	public void actionPerformed(final ActionEvent e) {
		System.out.println("WorkspaceRefreshAction");
		
		WorkspaceController.getController().reloadWorkspace();	
	}	
	
	
}
