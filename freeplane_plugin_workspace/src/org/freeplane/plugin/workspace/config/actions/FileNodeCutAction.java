package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

public class FileNodeCutAction extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FileNodeCutAction() {
		super("FileNodeCutAction");
	}
	
	public void actionPerformed(final ActionEvent e) {
        System.out.println("FileNodeCutAction: "+e.getActionCommand()+" : "+e.getID());
    }


}
