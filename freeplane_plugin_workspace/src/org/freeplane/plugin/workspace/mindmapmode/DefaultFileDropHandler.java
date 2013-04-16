package org.freeplane.plugin.workspace.mindmapmode;

import java.awt.datatransfer.Transferable;

import org.freeplane.plugin.workspace.dnd.INodeDropHandler;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public class DefaultFileDropHandler implements INodeDropHandler {

	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public boolean processDrop(AWorkspaceTreeNode targetNode, Transferable transferable, int dropAction) {
		return false;
	}

	public boolean acceptDrop(Transferable transferable) {
		return false;
	}
}
