/**
 * author: Marcel Genzmehr
 * 03.02.2012
 */
package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.plugin.workspace.io.IFileSystemRepresentation;
import org.freeplane.plugin.workspace.model.action.AWorkspaceAction;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;


/**
 * PhysicalFolderSortOrderAction
 */
@EnabledAction(checkOnPopup = true)
@SelectableAction(checkOnPopup = true)
public class PhysicalFolderSortOrderAction extends AWorkspaceAction {	
	
	private static final long serialVersionUID = 1L;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public PhysicalFolderSortOrderAction() {
		super("workspace.action.node.physical.sort");
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public void setSelectedFor(AWorkspaceTreeNode node) {
		if(node instanceof IFileSystemRepresentation) {
			if(((IFileSystemRepresentation) node).orderDescending()) {
				setSelected(true);
				return;
			} 				
		}
		setSelected(false);
	}
	
	public void setEnabledFor(AWorkspaceTreeNode node) {
		if(!(node instanceof IFileSystemRepresentation)) {
			setEnabled(false);
		}
		else{
			setEnabled();
		}
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	/**
	 * 
	 */
	public void actionPerformed(ActionEvent e) {
		AWorkspaceTreeNode targetNode = getNodeFromActionEvent(e);
		if(targetNode instanceof IFileSystemRepresentation) {
			if(this.isSelected()==((IFileSystemRepresentation) targetNode).orderDescending()) {
				((IFileSystemRepresentation) targetNode).orderDescending(!this.isSelected());
				targetNode.refresh();
			}
		}
	}
}
