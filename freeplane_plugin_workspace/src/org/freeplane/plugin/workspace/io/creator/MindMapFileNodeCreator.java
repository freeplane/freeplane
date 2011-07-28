/**
 * author: Marcel Genzmehr
 * 21.07.2011
 */
package org.freeplane.plugin.workspace.io.creator;

import java.io.File;

import org.freeplane.plugin.workspace.config.node.WorkspaceNode;
import org.freeplane.plugin.workspace.io.node.MindMapFileNode;

/**
 * 
 */
public class MindMapFileNodeCreator extends FileNodeCreator {

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	/**
	 * @param tree
	 */
	public MindMapFileNodeCreator() {
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public WorkspaceNode getNode(String name, File file) {
		MindMapFileNode node = new MindMapFileNode(name, file);
		return node;
	}
	
}
