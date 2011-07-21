/**
 * author: Marcel Genzmehr
 * 21.07.2011
 */
package org.freeplane.plugin.workspace.io.creator;

import java.io.File;

import org.freeplane.core.ui.IndexedTree;
import org.freeplane.plugin.workspace.config.node.WorkspaceNode;

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
		// TODO Auto-generated constructor stub
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public WorkspaceNode getNode(String id, File file) {
		// TODO Auto-generated method stub
		return null;
	}
}
