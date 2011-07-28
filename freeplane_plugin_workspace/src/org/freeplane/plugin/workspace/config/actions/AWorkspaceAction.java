package org.freeplane.plugin.workspace.config.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.plugin.workspace.view.WorkspacePopupMenu;

public abstract class AWorkspaceAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AWorkspaceAction(String key, String title, Icon icon) {
		super(key, title, icon);		
	}

	public AWorkspaceAction(String key) {
		super(key);
	}
	
	protected DefaultMutableTreeNode getNodeFromActionEvent(ActionEvent e) {
		WorkspacePopupMenu pop = (WorkspacePopupMenu)((Component) e.getSource()).getParent();
		int x = pop.getInvokerLocation().x;
		int y = pop.getInvokerLocation().y;
		JTree tree = (JTree)pop.getInvoker();
		TreePath path = tree.getPathForLocation(x, y);	
		return (DefaultMutableTreeNode)path.getLastPathComponent();
	}

}
