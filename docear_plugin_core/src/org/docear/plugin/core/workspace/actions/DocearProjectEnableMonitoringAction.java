/**
 * author: Marcel Genzmehr
 * 21.11.2011
 */
package org.docear.plugin.core.workspace.actions;

import java.awt.event.ActionEvent;

import org.docear.plugin.core.workspace.node.FolderTypeProjectsNode;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

@SelectableAction(checkOnPopup=true)
public class DocearProjectEnableMonitoringAction extends AWorkspaceAction {

	private static final long serialVersionUID = 1L;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public DocearProjectEnableMonitoringAction() {
		super("workspace.action.docear.project.enable.monitoring");
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public void setSelectedFor(AWorkspaceTreeNode targetNode) {
		if(targetNode instanceof FolderTypeProjectsNode) {
			setSelected(((FolderTypeProjectsNode) targetNode).isMonitoring());
		} 
		else {
			setSelected(false);
		}
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		AWorkspaceTreeNode targetNode = getNodeFromActionEvent(e);
		if(targetNode instanceof FolderTypeProjectsNode) {
			((FolderTypeProjectsNode) targetNode).enableMonitoring(!((FolderTypeProjectsNode) targetNode).isMonitoring());
		}
	}
}
