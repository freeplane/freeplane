package org.freeplane.plugin.workspace.actions;

import java.awt.event.ActionEvent;

import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.IWorkspaceView;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public class NodeRefreshAction extends AWorkspaceAction {

	public static final String KEY = "workspace.action.node.refresh";
	private static final long serialVersionUID = 1L;

	public NodeRefreshAction() {
		super(KEY);
	}

	public void actionPerformed(final ActionEvent e) {		
		AWorkspaceTreeNode[] targetNodes = getSelectedNodes(e);
		for (AWorkspaceTreeNode targetNode : targetNodes) {
			if(targetNode == null) {
				targetNode = WorkspaceController.getCurrentModel().getRoot();
			}
			else {
				targetNode.refresh();
			}
		}
		IWorkspaceView view = WorkspaceController.getCurrentModeExtension().getView();
		if(view != null){
			view.refreshView();
		}
	}	
	
	
}
