/**
 * author: Marcel Genzmehr
 * 18.08.2011
 */
package org.docear.plugin.core.workspace.creator;

import org.docear.plugin.core.workspace.node.FolderTypeLibraryNode;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.model.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

/**
 * 
 */
public class FolderTypeLibraryCreator extends AWorkspaceNodeCreator {
	public static final String FOLDER_TYPE_LIBRARY = "library";
	
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
		String type = data.getAttribute("type", FOLDER_TYPE_LIBRARY);
		FolderTypeLibraryNode node = new FolderTypeLibraryNode(type);
		//TODO: add missing attribute handling

		String name = data.getAttribute("name", null);
		if(name == null) {
			return null;
		}
		node.setName(name);
		return node;
	}
}
