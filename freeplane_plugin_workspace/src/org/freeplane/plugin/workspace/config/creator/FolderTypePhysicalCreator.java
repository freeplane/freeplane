package org.freeplane.plugin.workspace.config.creator;

import java.io.File;
import java.net.URI;

import org.freeplane.core.util.LogUtils;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.node.AFolderNode;
import org.freeplane.plugin.workspace.config.node.PhysicalFolderNode;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public class FolderTypePhysicalCreator extends AWorkspaceNodeCreator {

	public FolderTypePhysicalCreator() {
	}

	public AWorkspaceTreeNode getNode(XMLElement data) {

		String type = data.getAttribute("type", AFolderNode.FOLDER_TYPE_PHYSICAL);
		PhysicalFolderNode node = new PhysicalFolderNode(type);
		
		String path = data.getAttribute("path", null);
		if (path == null) {
			return null;
		}
		node.setPath(URI.create(path));

		File file = WorkspaceUtils.resolveURI(node.getPath());
		if (file == null) {
			return null;
		}

		if (!file.exists()) {
			if (file.mkdirs()) {
				LogUtils.info("New Filesystem Folder Created: " + file.getAbsolutePath());
			}
		}

		String name = data.getAttribute("name", file.getName());
		node.setName(name);

		return node;
	}

	public void endElement(final Object parent, final String tag, final Object currentNode, final XMLElement lastBuiltElement) {
		super.endElement(parent, tag, currentNode, lastBuiltElement);
		if (currentNode == null) {
			return;
		}

		WorkspaceController
				.getController()
				.getFilesystemReader()
				.scanFileSystem((AWorkspaceTreeNode) currentNode,
						WorkspaceUtils.resolveURI(((PhysicalFolderNode) currentNode).getPath()));

	}

}
