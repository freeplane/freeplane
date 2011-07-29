/**
 * author: Marcel Genzmehr
 * 21.07.2011
 */
package org.freeplane.plugin.workspace.io.creator;

import java.io.File;

import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;
import org.freeplane.plugin.workspace.io.node.DefaultFileNode;

/**
 * 
 */
public class DefaultFileNodeCreator extends AFileNodeCreator {

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	/**
	 * @param tree
	 */
	public DefaultFileNodeCreator() {
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public AWorkspaceNode getNode(String name, File file) {
		DefaultFileNode node = new DefaultFileNode(name, file);
		return node;
	}
	
}
