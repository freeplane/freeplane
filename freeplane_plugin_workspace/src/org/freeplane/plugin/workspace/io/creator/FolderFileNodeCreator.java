/**
 * author: Marcel Genzmehr
 * 22.07.2011
 */
package org.freeplane.plugin.workspace.io.creator;

import java.io.File;

import org.freeplane.core.ui.IndexedTree;
import org.freeplane.plugin.workspace.config.node.WorkspaceNode;
import org.freeplane.plugin.workspace.io.node.FolderFileNode;

/**
 * 
 */
public class FolderFileNodeCreator extends FileNodeCreator {

	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	/**
	 * @param tree
	 */
	public FolderFileNodeCreator(IndexedTree tree) {
		super(tree);
		// TODO Auto-generated constructor stub
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public WorkspaceNode getNode(String name, File file) {
		FolderFileNode node = new FolderFileNode(file.getName(), file);
		return node;
	}
}
