package org.freeplane.plugin.workspace.config.creator;

import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.node.WorkspaceRoot;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public abstract class AWorkspaceNodeCreator implements IElementDOMHandler {
	
	abstract public AWorkspaceTreeNode getNode(final XMLElement data);
	
			
	public AWorkspaceNodeCreator() {
	}
	
	public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
		if (attributes == null) {
			return null;
		}		
		
		AWorkspaceTreeNode node = getNode(attributes);		
		if (node == null) { 
			return null;
		}
		node.setParent((AWorkspaceTreeNode) parent);
		node.setMandatoryAttributes(attributes);
		node.initializePopup();
			
		if (!WorkspaceUtils.getModel().containsNode(node.getKey())) {
			if(node instanceof WorkspaceRoot) {
				WorkspaceUtils.getModel().setRoot(node);
			} 
			else {
				WorkspaceUtils.getModel().addNodeTo(node, (AWorkspaceTreeNode) parent);
			}
			
		}
		return node;
	}

	public void endElement(final Object parent, final String tag, final Object userObject, final XMLElement lastBuiltElement) {
	}	
}