/**
 * author: Marcel Genzmehr
 * 18.08.2011
 */
package org.docear.plugin.core.workspace.creator;

import java.net.URI;

import org.docear.plugin.core.CoreConfiguration;
import org.docear.plugin.core.workspace.actions.LocationDialog;
import org.docear.plugin.core.workspace.node.FolderTypeLiteratureRepositoryNode;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.model.creator.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

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
		
		if (path == null || path.length()==0) {
			URI uri = CoreConfiguration.repositoryPathObserver.getUri();
			
			if (uri == null) {
				LocationDialog.showWorkspaceChooserDialog();	
			}
			else {
				node.setPath(uri);
			}
			return node;
		}
		
		node.setPath(URI.create(path));

		return node;
	}

	public void endElement(final Object parent, final String tag, final Object node, final XMLElement lastBuiltElement) {
		super.endElement(parent, tag, node, lastBuiltElement);
		if (node == null || ((FolderTypeLiteratureRepositoryNode) node).getPath() == null) {
			return;
		}
		if (node instanceof FolderTypeLiteratureRepositoryNode && ((FolderTypeLiteratureRepositoryNode) node).getChildCount() == 0) {
			WorkspaceController
					.getController()
					.getFilesystemMgr()
					.scanFileSystem((AWorkspaceTreeNode) node,
							WorkspaceUtils.resolveURI(((FolderTypeLiteratureRepositoryNode) node).getPath()));
		}

	}
}
