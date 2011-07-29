package org.freeplane.plugin.workspace.config.creator;

import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.config.node.GroupNode;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;

public class GroupCreator extends AConfigurationNodeCreator {

	public GroupCreator() {
	}

	@Override
	public AWorkspaceNode getNode(String id, XMLElement data) {
		GroupNode node = new GroupNode(id);
		String name = data.getAttribute("name", null);
		node.setName(name==null? "group" : name);
		return node;
	}
}
