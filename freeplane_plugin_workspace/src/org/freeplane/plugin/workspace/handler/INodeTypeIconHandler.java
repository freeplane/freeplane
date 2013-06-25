/**
 * author: Marcel Genzmehr
 * 27.12.2011
 */
package org.freeplane.plugin.workspace.handler;

import javax.swing.Icon;

import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

/**
 * 
 */
public interface INodeTypeIconHandler {
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	public Icon getIconForNode(AWorkspaceTreeNode node);
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
