/**
 * author: Marcel Genzmehr
 * 18.08.2011
 */
package org.docear.plugin.core.workspace.creator;

import java.io.File;
import java.net.URI;

import org.docear.plugin.core.workspace.node.FolderTypeProjectsNode;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.creator.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

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
	public AWorkspaceTreeNode getNode(XMLElement data) {
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
		node.setPath(uri);
		
		File file = WorkspaceUtils.resolveURI(node.getPath());
		if (!file.exists()) {
			file.mkdirs();			
		}
		boolean monitor = Boolean.parseBoolean(data.getAttribute("monitor", "false"));
		node.enableMonitoring(monitor);
		
		return node;
	}
	
	public void endElement(final Object parent, final String tag, final Object node, final XMLElement lastBuiltElement) {
		super.endElement(parent, tag, node, lastBuiltElement);
		if (node == null) {
			return;
		}
		if (node instanceof FolderTypeProjectsNode && ((FolderTypeProjectsNode) node).getChildCount() == 0) {
			WorkspaceController
					.getController()
					.getFilesystemReader()
					.scanFileSystem((AWorkspaceTreeNode) node,
							WorkspaceUtils.resolveURI(((FolderTypeProjectsNode) node).getPath()));
		}
	}
}
