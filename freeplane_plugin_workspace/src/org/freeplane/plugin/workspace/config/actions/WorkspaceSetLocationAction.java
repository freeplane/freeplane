package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;

public class WorkspaceSetLocationAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WorkspaceSetLocationAction() {
		super("WorkspaceSetLocationAction");
	}
	
	public void actionPerformed(final ActionEvent e) {
        System.out.println("WorkspaceSetLocationAction: "+e.getActionCommand()+" : "+e.getID());
    }


}
