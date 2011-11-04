/**
 * author: Marcel Genzmehr
 * 18.08.2011
 */
package org.docear.plugin.core.workspace.creator;

import java.io.File;
import java.net.URI;

import org.docear.plugin.core.workspace.node.FolderTypeLiteratureRepositoryNode;
import org.freeplane.core.util.LogUtils;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.creator.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

/**
 * 
 */
public class FolderTypeLiteratureRepositoryCreator extends AWorkspaceNodeCreator {

	public static final String FOLDER_TYPE_LITERATUREREPOSITORY = "literature_repository";

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public AWorkspaceTreeNode getNode(XMLElement data) {
		String type = data.getAttribute("type", FOLDER_TYPE_LITERATUREREPOSITORY);
		FolderTypeLiteratureRepositoryNode node = new FolderTypeLiteratureRepositoryNode(type);
		// TODO: add missing attribute handling
		String path = data.getAttribute("path", null);
		if (path == null) {
			return null;
		}
		node.setFolderPath(URI.create(path));

		File file = WorkspaceUtils.resolveURI(node.getFolderPath());

		if (file != null) {
			if (!file.exists()) {
				if (file.mkdirs()) {
					LogUtils.info("New Filesystem Folder Created: " + file.getAbsolutePath());
				}
			}
			node.setName(file.getName());
		}
		else {
			node.setName("no folder selected!");
		}
		return node;
	}

	public void endElement(final Object parent, final String tag, final Object node, final XMLElement lastBuiltElement) {
		super.endElement(parent, tag, node, lastBuiltElement);
		if (node == null) {
			return;
		}
		if (node instanceof FolderTypeLiteratureRepositoryNode && ((FolderTypeLiteratureRepositoryNode) node).getChildCount() == 0) {
			WorkspaceController
					.getController()
					.getFilesystemReader()
					.scanFileSystem((AWorkspaceTreeNode) node,
							WorkspaceUtils.resolveURI(((FolderTypeLiteratureRepositoryNode) node).getFolderPath()));
		}

	}
}
