package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;

public class FileNodePasteAction extends AFreeplaneAction {

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
