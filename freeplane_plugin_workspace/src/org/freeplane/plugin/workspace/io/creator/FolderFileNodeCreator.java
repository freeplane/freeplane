/**
 * author: Marcel Genzmehr
 * 22.07.2011
 */
package org.freeplane.plugin.workspace.io.creator;

import java.io.File;

import org.freeplane.plugin.workspace.io.node.FolderFileNode;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

/**
 * 
 */
public class FolderFileNodeCreator extends AFileNodeCreator {

	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	/**
	 * @param tree
	 */
	public FolderFileNodeCreator() {
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public AWorkspaceTreeNode getNode(String name, File file, String fileExtension) {
		FolderFileNode node = new FolderFileNode(file.getName(), file);
		node.setFileExtension(fileExtension);
		return node;
	}
}
