/**
 * author: Marcel Genzmehr
 * 21.07.2011
 */
package org.freeplane.plugin.workspace.io.creator;

import java.io.File;

import org.freeplane.plugin.workspace.config.node.WorkspaceNode;
import org.freeplane.plugin.workspace.io.node.DefaultFileNode;

/**
 * 
 */
public class DefaultFileNodeCreator extends FileNodeCreator {

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
	
	public WorkspaceNode getNode(String name, File file) {
		DefaultFileNode node = new DefaultFileNode(name, file);
		return node;
	}
	
}
