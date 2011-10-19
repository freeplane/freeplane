/**
 * author: Marcel Genzmehr
 * 18.08.2011
 */
package org.docear.plugin.core.workspace.node;

import java.net.URI;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.IBibtexDatabase;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.freeplane.plugin.workspace.config.node.LinkTypeFileNode;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;

/**
 * 
 */
public class LinkTypeReferencesNode extends LinkTypeFileNode /*LinkNode*/ implements IBibtexDatabase {	
	private static final Icon DEFAULT_ICON = new ImageIcon(LinkTypeReferencesNode.class.getResource("/images/text-x-bibtex.png"));	
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public LinkTypeReferencesNode(String type) {
		super(type);
		DocearEvent event = new DocearEvent(this, DocearEventType.LIBRARY_NEW_REFERENCES_INDEXING_REQUEST, this);
		DocearController.getController().dispatchDocearEvent(event);
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		renderer.setOpenIcon(DEFAULT_ICON);
		renderer.setClosedIcon(DEFAULT_ICON);
		renderer.setLeafIcon(DEFAULT_ICON);
		return true;
	}
	
	public void handleEvent(WorkspaceNodeEvent event) {
		if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {			
			//TODO: DOCEAR implement popup handling if needed
		} 
		else {
			super.handleEvent(event);
		}
	}

	
	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	public URI getUri() {
		return this.getLinkPath();
	}
}
