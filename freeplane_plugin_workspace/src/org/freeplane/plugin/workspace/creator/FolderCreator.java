/**
 * author: Marcel Genzmehr
 * 16.08.2011
 */
package org.freeplane.plugin.workspace.creator;

import java.util.Hashtable;

import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.model.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

/**
 * 
 */
public class FolderCreator extends AWorkspaceNodeCreator {

	private Hashtable<String, AWorkspaceNodeCreator> creatorTable = new Hashtable<String, AWorkspaceNodeCreator>();
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	public void addTypeCreator(final String typeName, final AWorkspaceNodeCreator creator) {
		if(creatorTable.containsKey(typeName)) {
			creatorTable.remove(typeName);
		}
		creatorTable.put(typeName, creator);
	}
	
	
	public void endElement(final Object parent, final String tag, final Object userObject, final XMLElement lastBuiltElement) {
		String type = lastBuiltElement.getAttribute("type", null);
		if(type == null || !creatorTable.containsKey(type)) {
			return;
		}
		
		AWorkspaceNodeCreator creator = creatorTable.get(type);
		creator.endElement(parent, tag, userObject, lastBuiltElement);
	}
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	public AWorkspaceTreeNode getNode(XMLElement data) {
		String type = data.getAttribute("type", null);
		if(type == null || !creatorTable.containsKey(type)) {
			return null;
		}
		
		AWorkspaceNodeCreator creator = creatorTable.get(type);
		AWorkspaceTreeNode node = creator.getNode(data);
		return node;
	}
	
	
}
