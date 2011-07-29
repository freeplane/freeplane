package org.freeplane.plugin.workspace.config.creator;

import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;
import org.freeplane.plugin.workspace.config.node.WorkspaceRoot;

public class WorkspaceCreator extends AConfigurationNodeCreator {
	public WorkspaceCreator() {
	}

	public AWorkspaceNode getNode(String id, XMLElement data) {		
		WorkspaceRoot node = new WorkspaceRoot(id);
		String name = data.getAttribute("name", null);
		node.setName(name==null? "workspace" : name);
		return node;
	}
}
