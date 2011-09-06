/**
 * author: Marcel Genzmehr
 * 16.08.2011
 */
package org.freeplane.plugin.workspace.config.node;


/**
 * 
 */
public class FolderNode extends AWorkspaceNode {
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	final public static String FOLDER_TYPE_PHYSICAL = "physical";
	final public static String FOLDER_TYPE_VIRTUAL = "virtual";

	/**
	 * @param type
	 */
	public FolderNode(String type) {
		super(type);
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public final String getTagName() {
		return "folder";
	}
}
