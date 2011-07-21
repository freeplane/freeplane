package org.freeplane.plugin.workspace.io.node;

import java.io.File;

import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.io.PhysicalNode;

public class ImageFileNode extends PhysicalNode implements IWorkspaceNodeEventListener {
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public ImageFileNode(String name, File file) {
		super(name, file);
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
		System.out.println("ImageFileNode: "+ event);
	}

}
