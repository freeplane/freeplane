/**
 * author: Marcel Genzmehr
 * 17.11.2011
 */
package org.freeplane.plugin.workspace.actions;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.SelectableAction;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.PhysicalFolderNode;

@SelectableAction(checkOnPopup=true)
public class NodeEnableMonitoringAction extends AWorkspaceAction {
	
	private static final long serialVersionUID = 1L;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public NodeEnableMonitoringAction() {
		super("workspace.action.node.enable.monitoring");
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void actionPerformed(ActionEvent e) {
		AWorkspaceTreeNode targetNode = getNodeFromActionEvent(e);
		if(targetNode instanceof PhysicalFolderNode) {
			((PhysicalFolderNode) targetNode).enableMonitoring(!((PhysicalFolderNode) targetNode).isMonitoring());
		}
	}
	
	public void setSelectedFor(AWorkspaceTreeNode targetNode) {
		if(targetNode instanceof PhysicalFolderNode) {
			setSelected(((PhysicalFolderNode) targetNode).isMonitoring());
		} 
		else {
			setSelected(false);
		}
	}
}
