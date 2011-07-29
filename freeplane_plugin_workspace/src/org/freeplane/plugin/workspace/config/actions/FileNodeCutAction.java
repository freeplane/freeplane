package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;

public class FileNodeCutAction extends AFreeplaneAction {

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
