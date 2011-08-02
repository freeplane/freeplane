package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;
import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.plugin.workspace.WorkspaceController;

public class WorkspaceCollapseAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WorkspaceCollapseAction() {
		super("WorkspaceCollapseAction");
	}

	public void actionPerformed(final ActionEvent e) {
		System.out.println("WorkspaceCollapseAction: "+e.getActionCommand()+" : "+e.getID());
		JTree workspaceTree = WorkspaceController.getCurrentWorkspaceController().getWorspaceTree();
		collapseAll(workspaceTree);
	}

	private static void collapseAll(JTree tree) {
	    TreeNode root = (TreeNode) tree.getModel().getRoot();
	 
	    TreePath rootPath = new TreePath(root);
	    for (Enumeration<?> e=root.children(); e.hasMoreElements();) {
	    	TreeNode n = (TreeNode) e.nextElement();
	    	TreePath path = rootPath.pathByAddingChild(n);
	    	collapseAll(tree, path);
	    }
	}
	 
	private static void collapseAll(JTree tree, TreePath parent) {
	    TreeNode node = (TreeNode) parent.getLastPathComponent();
	    if (node.getChildCount() >= 0) {
	        for (Enumeration<?> e=node.children(); e.hasMoreElements();) {
	            TreeNode n = (TreeNode) e.nextElement();
	            TreePath path = parent.pathByAddingChild(n);
	            collapseAll(tree, path);
	        }
	    }	    
	    tree.collapsePath(parent);	 
	}
}
