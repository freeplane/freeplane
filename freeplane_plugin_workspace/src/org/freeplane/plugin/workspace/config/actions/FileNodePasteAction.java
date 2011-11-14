package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

public class FileNodePasteAction extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FileNodePasteAction() {
		super("FileNodePasteAction");
	}
	
	public void actionPerformed(final ActionEvent e) {
        System.out.println("FileNodePasteAction: "+e.getActionCommand()+" : "+e.getID());
    }


}
