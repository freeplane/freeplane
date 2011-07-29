package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;

public class WorkspaceHideAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WorkspaceHideAction() {
		super("WorkspaceHideAction");
	}
	
	public void actionPerformed(final ActionEvent e) {
        System.out.println("WorkspaceHideAction: "+e.getActionCommand()+" : "+e.getID());
    }


}
