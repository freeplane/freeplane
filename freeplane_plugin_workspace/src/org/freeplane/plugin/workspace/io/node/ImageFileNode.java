package org.freeplane.plugin.workspace.io.node;

import java.io.File;

import org.freeplane.plugin.workspace.controller.WorkspaceNodeAction;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

public class ImageFileNode extends DefaultFileNode {
	
	private static final long serialVersionUID = 1L;
	
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
	public void handleAction(WorkspaceNodeAction event) {
		System.out.println("ImageFileNode: "+ event);
		super.handleAction(event);
		
		
	}
}
