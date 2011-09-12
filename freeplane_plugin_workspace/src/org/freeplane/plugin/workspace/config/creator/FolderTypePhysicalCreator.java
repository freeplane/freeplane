package org.freeplane.plugin.workspace.config.creator;

import java.io.File;
import java.net.URI;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.util.LogUtils;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;
import org.freeplane.plugin.workspace.config.node.FolderNode;
import org.freeplane.plugin.workspace.config.node.PhysicalFolderNode;

public class FolderTypePhysicalCreator extends AWorkspaceNodeCreator {

	public FolderTypePhysicalCreator() {
	}

	public AWorkspaceNode getNode(XMLElement data) {

		String type = data.getAttribute("type", FolderNode.FOLDER_TYPE_PHYSICAL);
		PhysicalFolderNode node = new PhysicalFolderNode(type);
		//node.setMandatoryAttributes(data);
		
		String path = data.getAttribute("path", null);
		if (path == null) {
			return null;
		}
		node.setFolderPath(URI.create(path));

		File file = WorkspaceUtils.resolveURI(node.getFolderPath());
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

	public void endElement(final Object parent, final String tag, final Object userObject, final XMLElement lastBuiltElement) {
		final IndexedTree tree = WorkspaceController.getController().getIndexTree();
		super.endElement(parent, tag, userObject, lastBuiltElement);
		final Path path = (Path) userObject;
		if (path.path == null) {
			return;
		}
		final DefaultMutableTreeNode treeNode = tree.get(path.path);
		if (treeNode.getChildCount() == 0) {
			PhysicalFolderNode node = (PhysicalFolderNode) treeNode.getUserObject();
			WorkspaceController.getController().getFilesystemReader()
					.scanFilesystem(node, WorkspaceUtils.resolveURI(node.getFolderPath()));
		}

	}

}
