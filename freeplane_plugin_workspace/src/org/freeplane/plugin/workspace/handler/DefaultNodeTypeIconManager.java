/**
 * author: Marcel Genzmehr
 * 27.12.2011
 */
package org.freeplane.plugin.workspace.handler;

import java.util.HashMap;

import javax.swing.Icon;

import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

/**
 * 
 */
public class DefaultNodeTypeIconManager implements INodeTypeIconManager {

	private final HashMap<Class<? extends AWorkspaceTreeNode>, INodeTypeIconHandler> handlers= new HashMap<Class<? extends AWorkspaceTreeNode>, INodeTypeIconHandler>();

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	/* (non-Javadoc)
	 * @see org.freeplane.plugin.workspace.controller.INodeTypeIconManager#getIconForNode(org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode)
	 */
	public Icon getIconForNode(AWorkspaceTreeNode node) {
		INodeTypeIconHandler handler = handlers.get(node.getClass());
		if(handler != null) {
			return handler.getIconForNode(node);
		}
		return null;
	}

	public void addNodeTypeIconHandler(Class<? extends AWorkspaceTreeNode> type, INodeTypeIconHandler handler) {
		if(type == null || handler == null) {
			return;
		}
		handlers.put(type, handler);
	}

	public INodeTypeIconHandler removeNodeTypeIconHandler(Class<? extends AWorkspaceTreeNode> type) {
		if(type == null) {
			return null;
		}
		return handlers.remove(type);
	}
}
