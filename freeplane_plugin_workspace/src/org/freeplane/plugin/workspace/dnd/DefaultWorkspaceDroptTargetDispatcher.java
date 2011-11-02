/**
 * author: Marcel Genzmehr
 * 21.10.2011
 */
package org.freeplane.plugin.workspace.dnd;

import java.awt.dnd.DropTargetDropEvent;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.plugin.workspace.WorkspaceController;

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
		DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) WorkspaceController.getController().getWorkspaceViewTree()
				.getPathForLocation(event.getLocation().x, event.getLocation().y).getLastPathComponent();
		while(targetNode != null && targetNode.getUserObject() instanceof IDropAcceptor) {
			if(((IDropAcceptor)targetNode.getUserObject()).acceptDrop(event.getCurrentDataFlavors())) {
				return (IDropAcceptor)targetNode.getUserObject();
			}
			targetNode = (DefaultMutableTreeNode) targetNode.getParent();
			
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
