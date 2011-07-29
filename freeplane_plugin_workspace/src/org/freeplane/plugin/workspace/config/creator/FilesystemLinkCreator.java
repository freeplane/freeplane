package org.freeplane.plugin.workspace.config.creator;

import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.config.node.FilesystemLinkNode;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;

public class FilesystemLinkCreator extends AConfigurationNodeCreator {

	public FilesystemLinkCreator() {
	}

	@Override
	public AWorkspaceNode getNode(String id, XMLElement data) {
		FilesystemLinkNode node = new FilesystemLinkNode(id);
		String name = data.getAttribute("name", null);
		node.setName(name==null? "reference" : name);
		return node;
	}

}
