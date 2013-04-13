package org.freeplane.plugin.workspace.creator;

import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.model.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;

public class LinkTypeFileCreator extends AWorkspaceNodeCreator {

	public LinkTypeFileCreator() {
	}

	@Override
	public AWorkspaceTreeNode getNode(XMLElement data) {
		String type = data.getAttribute("type", "file");
		LinkTypeFileNode node = new LinkTypeFileNode(type);
		

		String path = data.getAttribute("path", null);
		if (path == null || path.length() == 0) {
			return null;
		}
		node.setLinkURI(URIUtils.createURI(path)); 		
		String name = data.getAttribute("name", URIUtils.getAbsoluteFile(node.getLinkURI()).getName());
		node.setName(name);
		
		return node;

	}
}
