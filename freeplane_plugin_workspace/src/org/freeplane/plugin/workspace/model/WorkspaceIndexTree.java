/**
 * author: Marcel Genzmehr
 * 31.10.2011
 */
package org.freeplane.plugin.workspace.model;

import java.util.Hashtable;

/**
 * 
 */
public class WorkspaceIndexTree {
	private WorkspaceTreeNode root = null;
	private Hashtable<WorkspaceTreePath, WorkspaceTreeNode> hashIndex = new Hashtable<WorkspaceTreePath, WorkspaceTreeNode>();
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public WorkspaceIndexTree() {	
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public void setRootNode(WorkspaceTreeNode node) {
		this.root = node;
	}
	
	public void insertNode(WorkspaceTreePath path, WorkspaceTreeNode node) {
		if(hashIndex.containsKey(path)) {  
			WorkspaceTreeNode parent = hashIndex.get(path);
			node.setParent(parent);
			if(hashIndex.containsKey(node.getTreePath())) {
				//TODO: maybe show info box that the node was not inserted because an identical node already exists
				return;
			}
			parent.addChildNode(node);
			hashIndex.put(node.getTreePath(), node);
		}
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
