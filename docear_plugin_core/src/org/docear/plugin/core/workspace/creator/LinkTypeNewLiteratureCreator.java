/**
 * author: Marcel Genzmehr
 * 19.08.2011
 */
package org.docear.plugin.core.workspace.creator;

import java.net.URI;

import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.config.creator.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;

/**
 * 
 */
public class LinkTypeNewLiteratureCreator extends AWorkspaceNodeCreator {
	public static final String LINK_TYPE_NEWLITERATURE = "new_literature";
		
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
		String type = data.getAttribute("type", LINK_TYPE_NEWLITERATURE);
		LinkTypeNewLiteratureNode node = new LinkTypeNewLiteratureNode(type);
		//TODO: add missing attribute handling
		String path = data.getAttribute("path", null);
		String name = data.getAttribute("name", null);
		if (path == null || name == null) {
			return null;
		}	
		node.setLinkPath(URI.create(path));		
		node.setName(name);
		return node;
	}
}
