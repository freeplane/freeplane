package org.freeplane.plugin.workspace.config.creator;

import org.freeplane.core.ui.IndexedTree;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.config.node.WorkspaceNode;
import org.freeplane.plugin.workspace.config.node.WorkspaceRoot;

public class WorkspaceCreator extends ConfigurationNodeCreator {
	public WorkspaceCreator(IndexedTree tree) {
		super(tree);
	}

	@Override
	public WorkspaceNode getNode(String id, XMLElement data) {		
		WorkspaceRoot node = new WorkspaceRoot(id);
		String name = data.getAttribute("name", null);
		node.setName(name==null? "workspace" : name);
		return node;
	}
}
