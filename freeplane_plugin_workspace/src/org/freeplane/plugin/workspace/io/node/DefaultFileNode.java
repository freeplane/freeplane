/**
 * author: Marcel Genzmehr
 * 21.07.2011
 */
package org.freeplane.plugin.workspace.io.node;

import java.io.File;

import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;

/**
 * 
 */
public class DefaultFileNode extends PhysicalNode {

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	/**
	 * @param name
	 * @param file
	 */
	public DefaultFileNode(String name, File file) {
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
	
	public void handleEvent(WorkspaceNodeEvent event) {
		// TODO Auto-generated method stub
		System.out.println("DefaultFileNode: "+ event);
	}

}
