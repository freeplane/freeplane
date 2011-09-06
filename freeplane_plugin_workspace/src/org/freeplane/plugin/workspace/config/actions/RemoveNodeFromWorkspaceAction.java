package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.freeplane.core.ui.components.JFreeplaneMenuItem;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.view.WorkspacePopupMenu;

public class RemoveNodeFromWorkspaceAction extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RemoveNodeFromWorkspaceAction() {
		super("RemoveNodeFromWorkspaceAction");
	}

	public void actionPerformed(final ActionEvent e) {
		DefaultMutableTreeNode node = this.getNodeFromActionEvent(e);		
		WorkspacePopupMenu menu = (WorkspacePopupMenu) ((JFreeplaneMenuItem) e.getSource()).getParent();
		JTree tree = (JTree) menu.getInvoker();
		DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
		
		treeModel.removeNodeFromParent(node);
		
		WorkspaceUtils.saveCurrentConfiguration();				
	}

}
