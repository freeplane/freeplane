package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;
import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.config.node.FilesystemFolderNode;

public class RefreshWorkspaceTree extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RefreshWorkspaceTree() {
		super("RefreshWorkspaceTree");
	}

	public void actionPerformed(final ActionEvent e) {
		System.out.println("RefreshWorkspaceTree");
		//DefaultMutableTreeNode root = (DefaultMutableTreeNode) WorkspaceController.getCurrentWorkspaceController().getViewModel().getRoot();
		
		WorkspaceController.getCurrentWorkspaceController().refreshWorkspace();
		//collapseAll(workspaceTree);
	}	
	
//	private void updateAll(DefaultMutableTreeNode node) {	    
//	    if (node.getUserObject() instanceof FilesystemFolderNode) {
//	    	((FilesystemFolderNode) node.getUserObject()).refreshFolder(node);
//	    	return;
//	    }
//	    for (Enumeration<?> e=node.children(); e.hasMoreElements();) {
//	    	DefaultMutableTreeNode child = (DefaultMutableTreeNode) e.nextElement();
//	    	updateAll(child);
//	    }
//	}
	
}
