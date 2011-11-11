package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

public class FileNodeCopyAction extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FileNodeCopyAction() {
		super("FileNodeCopyAction");
	}
	
	public void actionPerformed(final ActionEvent e) {
        System.out.println("FileNodeCopyAction: "+e.getActionCommand()+" : "+e.getID());
    }


}
