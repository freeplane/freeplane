/**
 * author: Marcel Genzmehr
 * 01.11.2011
 */
package org.freeplane.plugin.workspace.model;

/**
 * 
 */
public class WorkspaceTreePath {
	private WorkspaceTreePath parent = null;
	private String name = null; 
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public WorkspaceTreePath() {
		
	}
	
	public WorkspaceTreePath(String name) {
		this.name = name;
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	public void setParent(WorkspaceTreePath treePath) {
		parent = treePath;		
	}
	
	public WorkspaceTreePath getParent() {
		return parent;
	}

	public String getName() {
		return (getName() == null ? Long.toHexString(System.currentTimeMillis()).toUpperCase() : this.name);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return (parent == null ? parent.toString() : "") + "/" + getName();
	}
		
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
