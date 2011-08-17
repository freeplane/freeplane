package org.freeplane.plugin.workspace.config.creator;

import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;
import org.freeplane.plugin.workspace.config.node.WorkspaceRoot;

public class WorkspaceRootCreator extends AWorkspaceNodeCreator {
	public WorkspaceRootCreator() {
	}

	public AWorkspaceNode getNode(XMLElement data) {
		WorkspaceRoot node = new WorkspaceRoot();
		String name = data.getAttribute("name", "workspace");
		node.setName(name);
		return node;
	}
}
