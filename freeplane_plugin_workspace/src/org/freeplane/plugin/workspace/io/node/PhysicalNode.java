/**
 * author: Marcel Genzmehr
 * 21.07.2011
 */
package org.freeplane.plugin.workspace.io.node;

import java.io.File;

import org.freeplane.plugin.workspace.config.node.WorkspaceNode;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;

/**
 * 
 */
public abstract class PhysicalNode extends WorkspaceNode implements IWorkspaceNodeEventListener{
	
	private final File file;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	/**
	 * @param name
	 */
	public PhysicalNode(String name, File file) {
		super(file.getName());
		this.setName(name);
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
