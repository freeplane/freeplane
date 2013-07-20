/**
 * author: Marcel Genzmehr
 * 16.08.2011
 */
package org.freeplane.plugin.workspace.nodes;

import java.net.URI;

import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;



/**
 * 
 */
public abstract class AFolderNode extends AWorkspaceTreeNode {
	
	private static final long serialVersionUID = 1L;
	final public static String FOLDER_TYPE_PHYSICAL = "physical";
	final public static String FOLDER_TYPE_VIRTUAL = "virtual";
	
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/	

	/**
	 * @param type
	 */
	public AFolderNode(String type) {
		super(type);
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public abstract URI getPath();
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public boolean isLeaf() {
		return false;
	}
	
	public String getTagName() {
		return "folder";
	}
	
	public boolean getAllowsChildren() {
		return true;
	}
}
