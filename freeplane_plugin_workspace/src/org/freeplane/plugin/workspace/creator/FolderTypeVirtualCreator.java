package org.freeplane.plugin.workspace.creator;

import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.model.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.AFolderNode;
import org.freeplane.plugin.workspace.nodes.VirtualFolderNode;

public class FolderTypeVirtualCreator extends AWorkspaceNodeCreator {

	public FolderTypeVirtualCreator() {
	}

	@Override
	public AWorkspaceTreeNode getNode(XMLElement data) {		
		String name = data.getAttribute("name", "virtual folder");
		String type = data.getAttribute("type", AFolderNode.FOLDER_TYPE_VIRTUAL);
		VirtualFolderNode node = new VirtualFolderNode(type);
		node.setName(name);
		return node;
	}
}
