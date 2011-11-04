package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public class RemoveNodeFromWorkspaceAction extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RemoveNodeFromWorkspaceAction() {
		super("RemoveNodeFromWorkspaceAction");
	}

	public void actionPerformed(final ActionEvent e) {
		AWorkspaceTreeNode node = this.getNodeFromActionEvent(e);
		WorkspaceUtils.getModel().removeNodeFromParent(node);
		WorkspaceUtils.saveCurrentConfiguration();				
	}

}
