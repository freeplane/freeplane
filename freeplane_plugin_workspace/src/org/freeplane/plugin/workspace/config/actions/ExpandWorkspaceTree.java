package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import javax.swing.JTree;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.plugin.workspace.WorkspaceController;

public class ExpandWorkspaceTree extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExpandWorkspaceTree() {
		super("ExpandWorkspaceTree");
	}
	
	public void actionPerformed(final ActionEvent e) {
        JTree workspaceTree = WorkspaceController.getCurrentWorkspaceController().getWorspaceTree();
        for (int i = 1; i < workspaceTree.getRowCount(); i++) {
                 workspaceTree.expandRow(i);
        }       
    }
}
