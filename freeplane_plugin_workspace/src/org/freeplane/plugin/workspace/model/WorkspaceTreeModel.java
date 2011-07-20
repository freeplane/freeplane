package org.freeplane.plugin.workspace.model;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

public class WorkspaceTreeModel extends DefaultTreeModel {
	
	public WorkspaceTreeModel(TreeNode root, boolean asksAllowsChildren) {
		super(root, asksAllowsChildren);
	}
	
	public WorkspaceTreeModel(TreeNode root) {
		super(root, false);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
