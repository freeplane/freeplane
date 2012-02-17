/**
 * author: Marcel Genzmehr
 * 19.08.2011
 */
package org.docear.plugin.core.workspace.creator;

import java.net.URI;

import org.docear.plugin.core.workspace.node.LinkTypeIncomingNode;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.model.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

/**
 * 
 */
public class LinkTypeIncomingCreator extends AWorkspaceNodeCreator {
	public static final String LINK_TYPE_INCOMING = "incoming";
		
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
		String type = data.getAttribute("type", LINK_TYPE_INCOMING);
		LinkTypeIncomingNode node = new LinkTypeIncomingNode(type);
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
