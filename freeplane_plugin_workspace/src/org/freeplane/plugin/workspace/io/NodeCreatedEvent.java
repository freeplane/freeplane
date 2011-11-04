/**
 * author: Marcel Genzmehr
 * 26.10.2011
 */
package org.freeplane.plugin.workspace.io;

import org.freeplane.plugin.workspace.config.node.AFolderNode;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;


public class NodeCreatedEvent {
	public enum NodeCreatedType {
		NODE_TYPE_FOLDER, NODE_TYPE_FILE
	}
	private final AWorkspaceTreeNode node;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public NodeCreatedEvent(AWorkspaceTreeNode createdNode) {
		assert(createdNode != null);
		this.node = createdNode;
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public AWorkspaceTreeNode getCreatedNode() {
		return node;
	}
	
	public NodeCreatedType getType() {
		if(node instanceof AFolderNode) {
			return NodeCreatedType.NODE_TYPE_FOLDER;
		}
		return NodeCreatedType.NODE_TYPE_FILE;
	}

	public String toString() {
		return this.getClass().getSimpleName()+"[createdNode="+getCreatedNode()+";type="+getType()+"]";
	}
	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
