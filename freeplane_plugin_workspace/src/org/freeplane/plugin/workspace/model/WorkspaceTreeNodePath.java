/**
 * author: Marcel Genzmehr
 * 03.11.2011
 */
package org.freeplane.plugin.workspace.model;

import javax.swing.tree.TreePath;

import org.freeplane.lang.Destructable;

/**
 * 
 */
public class WorkspaceTreeNodePath extends TreePath implements Destructable {
	private static final long serialVersionUID = 4611314235642699349L;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/**
	 * @see TreePath#TreePath(Object)
	 */
	public WorkspaceTreeNodePath(AWorkspaceTreeNode singlePath) {
		super(singlePath);
	}

	/**
	 * @see TreePath#TreePath(TreePath,Object)
	 */
	public WorkspaceTreeNodePath(TreePath parent, AWorkspaceTreeNode lastElement) {
		super(parent, lastElement);
	}

	/**
	 * @see TreePath#TreePath()
	 */
	protected WorkspaceTreeNodePath() {
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public String toString() {
		return (getParentPath() != null ? getParentPath().toString() : "") + "/"
				+ ((AWorkspaceTreeNode) getLastPathComponent()).getId();
	}

	// public boolean equals(Object o) {
	// if (o == this)
	// return true;
	// if (o instanceof TreePath) {
	// TreePath oTreePath = (TreePath) o;
	//
	// if (getPathCount() != oTreePath.getPathCount())
	// return false;
	//
	// for (TreePath path = this; path != null; path = path.getParentPath()) {
	// if
	// (!(path.getLastPathComponent().equals(oTreePath.getLastPathComponent())))
	// {
	// return false;
	// }
	// oTreePath = oTreePath.getParentPath();
	// }
	// return true;
	// }
	// return false;
	// }

	public WorkspaceTreeNodePath getParentPath() {
		return (WorkspaceTreeNodePath) super.getParentPath();
	}

	public WorkspaceTreeNodePath pathByAddingChild(Object child) {
		if (child == null) {
			throw new NullPointerException("Null child not allowed");
		}

		return new WorkspaceTreeNodePath(this, (AWorkspaceTreeNode) child);
	}

	public AWorkspaceTreeNode getPathComponent(int element) {
		return (AWorkspaceTreeNode) super.getPathComponent(element);
	}

	public AWorkspaceTreeNode getLastPathComponent() {
		return (AWorkspaceTreeNode) super.getLastPathComponent();
	}
	
	public boolean equals(Object o) {
		if(o instanceof WorkspaceTreeNodePath) {
			return this.toString().equals(o.toString());
		}
		return super.equals(o);
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public void disassociateReferences() {

	}
}
