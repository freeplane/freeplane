package org.freeplane.plugin.workspace.actions;

import java.awt.event.ActionEvent;

import javax.swing.JTree;

import org.freeplane.plugin.workspace.WorkspaceController;

public class WorkspaceExpandAction extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WorkspaceExpandAction() {
		super("workspace.action.all.expand");
	}
	
	public void actionPerformed(final ActionEvent e) {
        JTree workspaceTree = WorkspaceController.getController().getWorkspaceViewTree();
        for (int i = 1; i < workspaceTree.getRowCount(); i++) {
                 workspaceTree.expandRow(i);
        }       
    }
}
