/**
 * author: Marcel Genzmehr
 * 31.10.2011
 */
package org.freeplane.plugin.workspace.model;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.tree.TreeNode;

import org.docear.lang.Destructable;

/**
 * 
 */
public class WorkspaceTreeNode implements TreeNode, Destructable {

	private WorkspaceTreeNode parent = null;
	private Vector<WorkspaceTreeNode> children = new Vector<WorkspaceTreeNode>();
	private boolean allowsChildren = true;
	private final WorkspaceTreePath path;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public WorkspaceTreeNode(WorkspaceTreePath path) {	
		this.path = path;
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	public void setParent(WorkspaceTreeNode node) {
		this.parent = node;
		path.setParent(parent.getTreePath());
	}

	public WorkspaceTreePath getTreePath() {
		return path;
	}


	public void allowChildren(boolean allow) {
		allowsChildren = allow;
	}
	
	public String getKey() {
		return path.toString();
	}
	
	public void addChildNode(WorkspaceTreeNode node) {
		node.setParent(this);
		children.add(node);
	}
	
	protected WorkspaceTreeNode clone(WorkspaceTreeNode node) {		
		node.allowChildren(this.getAllowsChildren());
		for(WorkspaceTreeNode child : this.children) {
			node.addChildNode(child.clone());
		}
		return node;
	}
	
	public WorkspaceTreeNode clone() {
		WorkspaceTreeNode node = new WorkspaceTreeNode(new WorkspaceTreePath(this.getTreePath().getName()));
		return clone(node);
	}
	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public TreeNode getChildAt(int childIndex) {
		return children.get(childIndex); 
	}

	public int getChildCount() {
		return children.size();
	}

	public TreeNode getParent() {
		return this.parent;
	}

	public int getIndex(TreeNode node) {
		return children.indexOf(node);
	}

	public boolean getAllowsChildren() {
		return allowsChildren;
	}

	public boolean isLeaf() {
		return allowsChildren  || (children.size() == 0);
	}
	
	public Enumeration<WorkspaceTreeNode> children() {
		return children.elements();
	}

	public void disassociateReferences() {
		for(WorkspaceTreeNode child : this.children) {
			child.disassociateReferences();
		}
		this.parent = null;
	}
}
