/**
 * author: Marcel Genzmehr
 * 18.08.2011
 */
package org.docear.plugin.core.workspace.creator;

import java.net.URI;

import org.docear.plugin.core.workspace.node.LinkTypeReferencesNode;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.creator.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;

/**
 * 
 */
public class LinkTypeReferencesCreator extends AWorkspaceNodeCreator {

	public static final String LINK_TYPE_REFERENCES = "references";

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
		String type = data.getAttribute("type", LINK_TYPE_REFERENCES);
		LinkTypeReferencesNode node = new LinkTypeReferencesNode(type);
		//TODO: add missing attribute handling
		String path = data.getAttribute("path", null);
		if (path == null) {
			return null;
		}	
		node.setLinkPath(URI.create(path)); 		
		String name = data.getAttribute("name", WorkspaceUtils.resolveURI(node.getLinkPath()).getName());
		node.setName(name);
		return node;
	}
}
