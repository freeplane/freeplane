/**
 * author: Marcel Genzmehr
 * 16.08.2011
 */
package org.freeplane.plugin.workspace.model.node;

import java.net.URI;



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
	
	
	public String getTagName() {
		return "folder";
	}
}
