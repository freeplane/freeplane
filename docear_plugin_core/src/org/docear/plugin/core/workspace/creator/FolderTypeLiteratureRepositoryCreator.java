/**
 * author: Marcel Genzmehr
 * 18.08.2011
 */
package org.docear.plugin.core.workspace.creator;

import java.io.File;
import java.net.URI;

import javax.swing.tree.DefaultMutableTreeNode;

import org.docear.plugin.core.workspace.node.FolderTypeLiteratureRepositoryNode;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.util.LogUtils;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.creator.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;

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
	
	public AWorkspaceNode getNode(XMLElement data) {
		String type = data.getAttribute("type", FOLDER_TYPE_LITERATUREREPOSITORY);
		FolderTypeLiteratureRepositoryNode node = new FolderTypeLiteratureRepositoryNode(type);
		//TODO: add missing attribute handling
		String path = data.getAttribute("path", null);
		if (path == null) {
			return null;
		}
		node.setFolderPath(URI.create(path));

		File file = WorkspaceUtils.resolveURI(node.getFolderPath());
		
		if(file != null) {
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
	
	public void endElement(final Object parent, final String tag, final Object userObject, final XMLElement lastBuiltElement) {
		final IndexedTree tree = WorkspaceController.getController().getIndexTree();
		super.endElement(parent, tag, userObject, lastBuiltElement);
		final Path path = (Path) userObject;
		if (path.path == null) {
			return;
		}
		final DefaultMutableTreeNode treeNode = tree.get(path.path);
		if (treeNode.getChildCount() == 0) {
			FolderTypeLiteratureRepositoryNode node = (FolderTypeLiteratureRepositoryNode) treeNode.getUserObject();
			WorkspaceController.getController().getFilesystemReader()
					.scanFilesystem(node, WorkspaceUtils.resolveURI(node.getFolderPath()));
		}

	}
}
