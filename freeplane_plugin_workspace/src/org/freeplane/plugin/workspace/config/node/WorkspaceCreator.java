package org.freeplane.plugin.workspace.config.node;

import org.freeplane.core.ui.IndexedTree;
import org.freeplane.n3.nanoxml.XMLElement;

public class WorkspaceCreator extends NodeCreator {
	public WorkspaceCreator(IndexedTree tree) {
		super(tree);
	}

	@Override
	public ConfigurationNode getNode(String id, XMLElement data) {		
		WorkspaceRoot node = new WorkspaceRoot(id);
		String name = data.getAttribute("name", null);
		node.setName(name==null? "workspace" : name);
		return node;
	}
}
