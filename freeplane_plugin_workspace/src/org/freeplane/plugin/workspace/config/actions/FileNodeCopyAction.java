package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;

public class FileNodeCopyAction extends AFreeplaneAction {

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
