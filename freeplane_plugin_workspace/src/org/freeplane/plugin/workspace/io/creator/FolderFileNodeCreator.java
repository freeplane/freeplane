/**
 * author: Marcel Genzmehr
 * 22.07.2011
 */
package org.freeplane.plugin.workspace.io.creator;

import java.io.File;

import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;
import org.freeplane.plugin.workspace.io.node.FolderFileNode;

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
	
	public AWorkspaceNode getNode(String name, File file) {
		System.out.println("FolderFileNodeCreator.getNode: "+name+" : "+file.toString());
		FolderFileNode node = new FolderFileNode(file.getName(), file);
		return node;
	}
}
