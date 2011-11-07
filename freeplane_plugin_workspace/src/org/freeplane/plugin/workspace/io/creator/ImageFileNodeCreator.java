/**
 * author: Marcel Genzmehr
 * 21.07.2011
 */
package org.freeplane.plugin.workspace.io.creator;

import java.io.File;

import org.freeplane.plugin.workspace.io.node.ImageFileNode;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

/**
 * 
 */
public class ImageFileNodeCreator extends AFileNodeCreator {

	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/**
	 * @param tree
	 */
	public ImageFileNodeCreator() {
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public AWorkspaceTreeNode getNode(String name, File file, String fileExtension) {
		ImageFileNode node = new ImageFileNode(name, file);
		node.setFileExtension(fileExtension);
		return node;
	}
}
