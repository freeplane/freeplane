package org.freeplane.plugin.workspace.actions;

import java.awt.event.ActionEvent;

import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public class NodeRefreshAction extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NodeRefreshAction() {
		super("workspace.action.node.refresh");
	}

	public void actionPerformed(final ActionEvent e) {
		AWorkspaceTreeNode targetNode = getNodeFromActionEvent(e);
		if(targetNode == null) {
			targetNode = (AWorkspaceTreeNode) WorkspaceUtils.getModel().getRoot();
		}
		else {
			targetNode.refresh();
		}
	}	
	
	
}
