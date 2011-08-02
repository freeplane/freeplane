package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;

public class AddNewFilesystemFolderAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AddNewFilesystemFolderAction() {
		super("AddNewFilesystemFolderAction");
	}
	
	public void actionPerformed(final ActionEvent e) {
        System.out.println("AddNewFilesystemFolderAction: "+e.getActionCommand()+" : "+e.getID());
        //TODO: User AddExistingFilesystemFolderAction --> why does new created folder not appear?
    }


}
