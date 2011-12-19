/**
 * author: Marcel Genzmehr
 * 18.08.2011
 */
package org.docear.plugin.core.workspace.creator;

import java.net.URI;

import org.docear.plugin.core.CoreConfiguration;
import org.docear.plugin.core.LocationDialog;
import org.docear.plugin.core.workspace.node.LinkTypeReferencesNode;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.model.creator.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

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
	
	public AWorkspaceTreeNode getNode(XMLElement data) {
		String type = data.getAttribute("type", LINK_TYPE_REFERENCES);
		LinkTypeReferencesNode node = new LinkTypeReferencesNode(type);
		//TODO: add missing attribute handling		
		String name = data.getAttribute("name", null);
		
		if (name==null || name.trim().length()==0) {
			name = "not yet set!";
		}
		node.setName(name);
		
		String path = data.getAttribute("path", null);
		if(path == null || path.trim().length() == 0) {
			URI uri = CoreConfiguration.referencePathObserver.getUri();
			if (uri == null) {
				LocationDialog.showWorkspaceChooserDialog();		    	
			}
			else {
				node.setLinkPath(uri);
			}
			return node;
		}
		
		node.setLinkPath(URI.create(path));
			
		
		return node;
	}
}
