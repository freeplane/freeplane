package org.freeplane.plugin.workspace.actions;

import java.awt.event.ActionEvent;

import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.IWorkspaceView;
import org.freeplane.plugin.workspace.components.TreeView;

public class WorkspaceExpandAction extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WorkspaceExpandAction() {
		super("workspace.action.all.expand");
	}
	
	public void actionPerformed(final ActionEvent e) {
        IWorkspaceView view = WorkspaceController.getCurrentModeExtension().getView();
        if(view instanceof TreeView) {
        	((TreeView) view).expandAll(getNodeFromActionEvent(e));
        }
    }
}
