/**
 * author: Marcel Genzmehr
 * 21.07.2011
 */
package org.freeplane.plugin.workspace.io;

import java.io.File;

import org.freeplane.plugin.workspace.config.node.WorkspaceNode;

/**
 * 
 */
public abstract class PhysicalNode extends WorkspaceNode{

	private final File file;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	/**
	 * @param name
	 */
	public PhysicalNode(String name, File file) {
		super(name);
		this.file = file;
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public File getFile() {
		return this.file;
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
