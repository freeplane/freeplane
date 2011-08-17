package org.freeplane.plugin.workspace.config.creator;

import java.net.URI;

import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;
import org.freeplane.plugin.workspace.config.node.LinkTypeFileNode;

public class LinkTypeFileCreator extends AWorkspaceNodeCreator {

	public LinkTypeFileCreator() {
	}

	@Override
	public AWorkspaceNode getNode(XMLElement data) {
		String type = data.getAttribute("type", "file");
		LinkTypeFileNode node = new LinkTypeFileNode(type);
		

		String path = data.getAttribute("path", null);
		if (path == null) {
			return null;
		}	
		node.setLinkPath(URI.create(path)); 		
		String name = data.getAttribute("name", WorkspaceUtils.resolveURI(node.getLinkPath()).getName());
		node.setName(name);
		
		return node;

	}
}
