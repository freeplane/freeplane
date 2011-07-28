package org.freeplane.plugin.workspace.config.creator;

import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.config.node.FilesystemLinkNode;
import org.freeplane.plugin.workspace.config.node.WorkspaceNode;

public class FilesystemLinkCreator extends ConfigurationNodeCreator {

	public FilesystemLinkCreator() {
	}

	@Override
	public WorkspaceNode getNode(String id, XMLElement data) {
		FilesystemLinkNode node = new FilesystemLinkNode(id);
		String name = data.getAttribute("name", null);
		node.setName(name==null? "reference" : name);
		return node;
	}

}
