/**
 * author: Marcel Genzmehr
 * 21.07.2011
 */
package org.freeplane.plugin.workspace.io.creator;

import java.io.File;

import org.freeplane.core.ui.IndexedTree;
import org.freeplane.plugin.workspace.config.node.WorkspaceNode;
import org.freeplane.plugin.workspace.io.node.ImageFileNode;

/**
 * 
 */
public class ImageFileNodeCreator extends FileNodeCreator {

	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/**
	 * @param tree
	 */
	public ImageFileNodeCreator(IndexedTree tree) {
		super(tree);
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public WorkspaceNode getNode(String name, File file) {
		ImageFileNode node = new ImageFileNode(name, file);
		return node;
	}
}
