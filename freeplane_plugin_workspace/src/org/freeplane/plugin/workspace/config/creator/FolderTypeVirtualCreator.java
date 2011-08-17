package org.freeplane.plugin.workspace.config.creator;

import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.config.node.VirtualFolderNode;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;

public class FolderTypeVirtualCreator extends AWorkspaceNodeCreator {

	public FolderTypeVirtualCreator() {
	}

	@Override
	public AWorkspaceNode getNode(XMLElement data) {		
		String name = data.getAttribute("name", "virtual folder");
		String type = data.getAttribute("type", VirtualFolderNode.WSNODE_FOLDER_TYPE_VIRTUAL);
		VirtualFolderNode node = new VirtualFolderNode(type);
		node.setName(name);
		return node;
	}
}
