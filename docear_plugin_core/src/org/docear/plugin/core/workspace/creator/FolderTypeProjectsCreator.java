/**
 * author: Marcel Genzmehr
 * 18.08.2011
 */
package org.docear.plugin.core.workspace.creator;

import java.io.File;
import java.net.URI;

import javax.swing.tree.DefaultMutableTreeNode;

import org.docear.plugin.core.workspace.node.FolderTypeProjectsNode;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.creator.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;

public class FolderTypeProjectsCreator extends AWorkspaceNodeCreator {
	public static final String FOLDER_TYPE_PROJECTS = "projects";
	
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
		String type = data.getAttribute("type", FOLDER_TYPE_PROJECTS);
		FolderTypeProjectsNode node = new FolderTypeProjectsNode(type);
		//TODO: add missing attribute handling

		String name = data.getAttribute("name", null);
		if(name == null) {
			return null;
		}
		node.setName(name);
		String path = data.getAttribute("path", "workspace:/projects");
		URI uri;
		try{
			uri = URI.create(path);
		}
		catch (Exception e) {
			uri = URI.create("workspace:/projects");
		}
		node.setPathURI(uri);
		
		File file = WorkspaceUtils.resolveURI(node.getPathURI());
		if (!file.exists()) {
			file.mkdirs();			
		}
		boolean monitor = Boolean.parseBoolean(data.getAttribute("monitor", "false"));
		node.enableMonitoring(monitor);
		
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
			FolderTypeProjectsNode node = (FolderTypeProjectsNode) treeNode.getUserObject();
			WorkspaceController.getController().getFilesystemReader()
					.scanFilesystem(node, WorkspaceUtils.resolveURI(node.getPathURI()));
		}
	}
}
