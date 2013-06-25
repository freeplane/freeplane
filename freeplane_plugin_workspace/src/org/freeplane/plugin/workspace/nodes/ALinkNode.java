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
public abstract class ALinkNode extends AWorkspaceTreeNode {
	
	private static final long serialVersionUID = 1L;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	public static final String LINK_TYPE_FILE = "file";

	/**
	 * @param type
	 */
	public ALinkNode(String type) {
		super(type);
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public abstract URI getLinkURI();
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public final String getTagName() {
		return "link";
	}
	
	public boolean getAllowsChildren() {
		return false;
	}

	@Override
	public void initializePopup() {		
	}
}
