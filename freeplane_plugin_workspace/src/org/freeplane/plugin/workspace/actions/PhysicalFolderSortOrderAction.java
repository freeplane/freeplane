/**
 * author: Marcel Genzmehr
 * 03.02.2012
 */
package org.freeplane.plugin.workspace.actions;

import java.awt.event.ActionEvent;

import javax.swing.tree.TreePath;

import org.freeplane.core.ui.SelectableAction;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;
import org.freeplane.plugin.workspace.io.IFileSystemRepresentation;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;


/**
 * PhysicalFolderSortOrderAction
 */
@CheckEnableOnPopup
@SelectableAction(checkOnPopup = true)
public class PhysicalFolderSortOrderAction extends AWorkspaceAction {	
	public static final String KEY = "workspace.action.node.physical.sort";
	private static final long serialVersionUID = 1L;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public PhysicalFolderSortOrderAction() {
		super(KEY);
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public void setSelectedFor(AWorkspaceTreeNode node, TreePath[] selectedPaths) {
		if(node instanceof IFileSystemRepresentation) {
			if(((IFileSystemRepresentation) node).orderDescending()) {
				setSelected(true);
				return;
			} 				
		}
		setSelected(false);
	}
	
	public void setEnabledFor(AWorkspaceTreeNode node, TreePath[] selectedPaths) {
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
