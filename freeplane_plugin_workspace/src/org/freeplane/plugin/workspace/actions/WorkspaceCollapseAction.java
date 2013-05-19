package org.freeplane.plugin.workspace.actions;

import java.awt.event.ActionEvent;
import java.util.Enumeration;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.IWorkspaceView;
import org.freeplane.plugin.workspace.model.WorkspaceTreeModel;

public class WorkspaceCollapseAction extends AWorkspaceAction {

	private static final long serialVersionUID = 1L;

	public WorkspaceCollapseAction() {
		super("workspace.action.all.collapse");
	}

	public void actionPerformed(final ActionEvent e) {
		collapseAll(getNodeFromActionEvent(e).getModel());
	}

	private static void collapseAll(WorkspaceTreeModel model) {
	    TreeNode root = (TreeNode) model.getRoot();
	    IWorkspaceView view = WorkspaceController.getCurrentModeExtension().getView();
	    TreePath rootPath = new TreePath(root);
	    for (Enumeration<?> e=root.children(); e.hasMoreElements();) {
	    	TreeNode n = (TreeNode) e.nextElement();
	    	TreePath path = rootPath.pathByAddingChild(n);
	    	collapseAll(view, model, path);
	    }
	}
	 
	private static void collapseAll(IWorkspaceView view, WorkspaceTreeModel model, TreePath parent) {
	    TreeNode node = (TreeNode) parent.getLastPathComponent();
	    if (node.getChildCount() >= 0) {
	        for (Enumeration<?> e=node.children(); e.hasMoreElements();) {
	            TreeNode n = (TreeNode) e.nextElement();
	            TreePath path = parent.pathByAddingChild(n);
	            collapseAll(view, model, path);
	        }
	    }	    
	    view.collapsePath(parent);	 
	}
}
