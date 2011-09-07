/**
 * author: Marcel Genzmehr
 * 16.08.2011
 */
package org.freeplane.plugin.workspace.config.node;

/**
 * 
 */
public class LinkNode extends AWorkspaceNode {
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	public static final String LINK_TYPE_FILE = "file";

	/**
	 * @param type
	 */
	public LinkNode(String type) {
		super(type);
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public final String getTagName() {
		return "link";
	}

	@Override
	public void initializePopup() {
		// TODO Auto-generated method stub
		
	}
}
