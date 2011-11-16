package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

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
		AWorkspaceTreeNode node = getNodeFromActionEvent(e);
		node.refresh();
	}	
	
	
}
