/**
 * author: Marcel Genzmehr
 * 22.07.2011
 */
package org.freeplane.plugin.workspace.io.node;

import java.io.File;

import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.io.PhysicalNode;

/**
 * 
 */
public class FolderFileNode extends PhysicalNode {

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	/**
	 * @param name
	 * @param file
	 */
	public FolderFileNode(String name, File file) {
		super(name, file);
		// TODO Auto-generated constructor stub
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	public String toString() {
		return this.getName();
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	@Override
	public void handleEvent(WorkspaceNodeEvent event) {
		// TODO Auto-generated method stub
		System.out.println("FolderFileNode: "+ event);
	}
}
