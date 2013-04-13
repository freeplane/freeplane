package org.freeplane.plugin.workspace.creator;

import java.io.File;

import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.AFolderNode;
import org.freeplane.plugin.workspace.nodes.FolderLinkNode;

public class FolderTypePhysicalCreator extends AWorkspaceNodeCreator {

	public FolderTypePhysicalCreator() {
	}

	public AWorkspaceTreeNode getNode(XMLElement data) {

		String type = data.getAttribute("type", AFolderNode.FOLDER_TYPE_PHYSICAL);
		FolderLinkNode node = new FolderLinkNode(type);
		
		String path = data.getAttribute("path", null);
		if (path == null) {
			return null;
		}
		node.setPath(URIUtils.createURI(path));

		File file = URIUtils.getAbsoluteFile(node.getPath());
		if (file == null) {
			return null;
		}

		boolean monitor = Boolean.parseBoolean(data.getAttribute("monitor", "false"));
		node.enableMonitoring(monitor);
		
		boolean descending = Boolean.parseBoolean(data.getAttribute("orderDescending", "false"));
		node.orderDescending(descending);
		
		String name = data.getAttribute("name", file.getName());
		node.setName(name);

		return node;
	}

	public void endElement(final Object parent, final String tag, final Object currentNode, final XMLElement lastBuiltElement) {
		super.endElement(parent, tag, currentNode, lastBuiltElement);
		if (currentNode == null) {
			return;
		}

		WorkspaceController.getFileSystemMgr().scanFileSystem((AWorkspaceTreeNode) currentNode,
						URIUtils.getAbsoluteFile(((FolderLinkNode) currentNode).getPath()));

	}

}
