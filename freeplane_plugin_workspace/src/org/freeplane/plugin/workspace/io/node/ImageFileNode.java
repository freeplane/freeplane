package org.freeplane.plugin.workspace.io.node;

import java.io.File;

import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public class ImageFileNode extends DefaultFileNode {
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public ImageFileNode(String name, File file) {
		super(name, file);
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public AWorkspaceTreeNode clone() {
		ImageFileNode node = new ImageFileNode(getName(), getFile());
		return clone(node);
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	@Override
	public void handleEvent(WorkspaceNodeEvent event) {
		System.out.println("ImageFileNode: "+ event);
		super.handleEvent(event);
		
		
	}
}
