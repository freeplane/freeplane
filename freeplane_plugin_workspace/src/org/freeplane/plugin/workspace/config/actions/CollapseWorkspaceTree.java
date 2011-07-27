package org.freeplane.plugin.workspace.config.actions;

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.clipboard.ClipboardController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.IMapSelection;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.WorkspaceController;

public class CollapseWorkspaceTree extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CollapseWorkspaceTree() {
		super("CollapseWorkspaceTree");
	}
	
	public void actionPerformed(final ActionEvent e) {
		System.out.println("ROOT: "+WorkspaceController.getCurrentWorkspaceController().getViewModel().getRoot());
        System.out.println("CollapseWorkspaceTree: "+e.getActionCommand()+" : "+e.getID());
        System.out.println(e.getSource());
        
        
    }


}
