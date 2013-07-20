/**
 * author: Marcel Genzmehr
 * 27.12.2011
 */
package org.freeplane.plugin.workspace.handler;

import javax.swing.Icon;

import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;


public interface INodeTypeIconManager {
	
	public Icon getIconForNode(AWorkspaceTreeNode node);
	public void addNodeTypeIconHandler(Class<? extends AWorkspaceTreeNode> type, INodeTypeIconHandler handler);
	public INodeTypeIconHandler removeNodeTypeIconHandler(Class<? extends AWorkspaceTreeNode> type);
}
