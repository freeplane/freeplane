/**
 * author: Marcel Genzmehr
 * 18.08.2011
 */
package org.docear.plugin.core.workspace.creator;

import java.net.URI;

import org.docear.plugin.core.CoreConfiguration;
import org.docear.plugin.core.LocationDialog;
import org.docear.plugin.core.workspace.node.FolderTypeProjectsNode;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.model.creator.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

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

		String name = data.getAttribute("name", null);		
		if(name == null) {
			return null;
		}		
		node.setName(name);
		
		boolean monitor = Boolean.parseBoolean(data.getAttribute("monitor", "false"));
		node.enableMonitoring(monitor);
		
		String path = data.getAttribute("path", null);
		if(path == null || path.trim().length() == 0) {
			URI uri = CoreConfiguration.projectPathObserver.getUri();
			if (uri == null) {
				LocationDialog dialog = new LocationDialog(true); 
		    	dialog.setVisible(true);		    	
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
		if (node == null) {
			return;
		}
		if (node instanceof FolderTypeProjectsNode && ((FolderTypeProjectsNode) node).getChildCount() == 0 && ((FolderTypeProjectsNode) node).getPath() != null) {
			WorkspaceController
					.getController()
					.getFilesystemMgr()
					.scanFileSystem((AWorkspaceTreeNode) node,
							WorkspaceUtils.resolveURI(((FolderTypeProjectsNode) node).getPath()));
		}
	}
}
