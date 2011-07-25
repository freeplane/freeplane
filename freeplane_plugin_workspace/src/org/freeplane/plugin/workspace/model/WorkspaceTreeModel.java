package org.freeplane.plugin.workspace.model;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.config.node.WorkspaceNode;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;

public class WorkspaceTreeModel extends DefaultTreeModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public WorkspaceTreeModel(TreeNode root, boolean asksAllowsChildren) {
		super(root, asksAllowsChildren);
	}
	
	public WorkspaceTreeModel(TreeNode root) {
		super(root, false);
	}
	
	public void nodeChanged(TreeNode node) {
		super.nodeChanged(node);
		LogUtils.info("wsNode changed: "+node);
	}
	
	public void valueForPathChanged(TreePath path, Object newValue) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		
		if (node.getUserObject() instanceof IWorkspaceNodeEventListener) {			
			((IWorkspaceNodeEventListener) node.getUserObject()).handleEvent(new WorkspaceNodeEvent(node, WorkspaceNodeEvent.WSNODE_CHANGED, newValue));
			nodeChanged(node);
		}
		else {
			((WorkspaceNode)node.getUserObject()).setName(newValue.toString());
		}		
	}



}
