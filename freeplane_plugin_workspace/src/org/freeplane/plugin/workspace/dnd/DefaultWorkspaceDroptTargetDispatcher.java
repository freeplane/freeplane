/**
 * author: Marcel Genzmehr
 * 21.10.2011
 */
package org.freeplane.plugin.workspace.dnd;

import java.awt.dnd.DropTargetDropEvent;

import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

/**
 * 
 */
public class DefaultWorkspaceDroptTargetDispatcher implements IDropTargetDispatcher {

	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	private IDropAcceptor getDropAcceptor(final DropTargetDropEvent event) {
		AWorkspaceTreeNode targetNode = (AWorkspaceTreeNode) WorkspaceController.getController().getWorkspaceViewTree()
				.getPathForLocation(event.getLocation().x, event.getLocation().y).getLastPathComponent();
		while(targetNode != null && targetNode instanceof IDropAcceptor) {
			if(((IDropAcceptor)targetNode).acceptDrop(event.getCurrentDataFlavors())) {
				return (IDropAcceptor)targetNode;
			}
			targetNode = targetNode.getParent();
			
		}
		return null;
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	/**
	 */
	public boolean dispatchDropEvent(DropTargetDropEvent event) {
		
		IDropAcceptor acceptor = getDropAcceptor(event);
		if(acceptor != null) {			
			return acceptor.processDrop(event);
		}
		return false;
	}
}
