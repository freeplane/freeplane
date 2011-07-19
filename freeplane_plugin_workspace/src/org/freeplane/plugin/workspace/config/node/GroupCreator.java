package org.freeplane.plugin.workspace.config.node;

import org.freeplane.core.ui.IndexedTree;
import org.freeplane.n3.nanoxml.XMLElement;

public class GroupCreator extends NodeCreator {

	public GroupCreator(IndexedTree tree) {
		super(tree);
	}

	@Override
	public ConfigurationNode getNode(String id, XMLElement data) {
		GroupNode node = new GroupNode(id);
		String name = data.getAttribute("name", null);
		node.setName(name==null? "group" : name);
		return node;
	}
}
