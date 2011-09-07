/**
 * author: Marcel Genzmehr
 * 18.08.2011
 */
package org.docear.plugin.core.workspace.node;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.freeplane.plugin.workspace.config.node.LinkTypeFileNode;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;

/**
 * 
 */
public class LinkTypeReferencesNode extends LinkTypeFileNode /*LinkNode*/ {	
	private static final Icon DEFAULT_ICON = new ImageIcon(LinkTypeReferencesNode.class.getResource("/images/text-x-bibtex.png"));	
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public LinkTypeReferencesNode(String type) {
		super(type);
		// TODO Auto-generated constructor stub
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
}
