package org.freeplane.plugin.workspace.dnd;

import java.io.IOException;

import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public class NoDropHandlerFoundExeption extends IOException {

	private static final long serialVersionUID = 1L;
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public NoDropHandlerFoundExeption(AWorkspaceTreeNode targetNode) {
		super("no drop handler has been registered for: "+ targetNode.getClass());
	}

	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
