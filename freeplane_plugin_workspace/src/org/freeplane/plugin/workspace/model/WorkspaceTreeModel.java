package org.freeplane.plugin.workspace.model;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;
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

	public Object getRoot() {
		return root.getChildAt(0);
	}

	public void nodeChanged(TreeNode node) {
		super.nodeChanged(node);
		LogUtils.info("wsNode changed: " + node);
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

		if (node.getUserObject() instanceof IWorkspaceNodeEventListener) {
			((IWorkspaceNodeEventListener) node.getUserObject()).handleEvent(new WorkspaceNodeEvent(node,
					WorkspaceNodeEvent.WSNODE_CHANGED, newValue));
			nodeChanged(node);
		}
		else {
			((AWorkspaceNode) node.getUserObject()).setName(newValue.toString());
		}
	}

	public Object getChild(Object parent, int index) {
		Object node = super.getChild(parent, index);
		return node;
	}

	/**
	 * Returns the number of children of <I>parent</I>. Returns 0 if the node is
	 * a leaf or if it has no children. <I>parent</I> must be a node previously
	 * obtained from this data source.
	 * 
	 * @param parent
	 *            a node in the tree, obtained from this data source
	 * @return the number of children of the node <I>parent</I>
	 */
	public int getChildCount(Object parent) {
		int count = ((TreeNode) parent).getChildCount();
		return count;
	}

	public void insertNodeInto(MutableTreeNode newChild, MutableTreeNode parent, int index) {
		parent.insert(newChild, index);

		int[] newIndexs = new int[1];

		newIndexs[0] = index;
		nodesWereInserted(parent, newIndexs);
	}

	public void removeNodeFromParent(MutableTreeNode node) {
		MutableTreeNode parent = (MutableTreeNode) node.getParent();

		if (parent == null)
			throw new IllegalArgumentException("node does not have a parent.");

		int[] childIndex = new int[1];
		Object[] removedArray = new Object[1];

		childIndex[0] = parent.getIndex(node);
		parent.remove(childIndex[0]);
		removedArray[0] = node;
		nodesWereRemoved(parent, childIndex, removedArray);
	}
	
	protected TreeNode[] getPathToRoot(TreeNode aNode, int depth) {
        TreeNode[]              retNodes;
	// This method recurses, traversing towards the root in order
	// size the array. On the way back, it fills in the nodes,
	// starting from the root and working back to the original node.

        /* Check for null, in case someone passed in a null node, or
           they passed in an element that isn't rooted at root. */
        if(aNode == null) {
            if(depth == 0)
                return null;
            else
                retNodes = new TreeNode[depth];
        }
        else {
            depth++;
            if(aNode == getRoot())
                retNodes = new TreeNode[depth];
            else
                retNodes = getPathToRoot(aNode.getParent(), depth);
            retNodes[retNodes.length - depth] = aNode;
        }
        return retNodes;
    }

}
