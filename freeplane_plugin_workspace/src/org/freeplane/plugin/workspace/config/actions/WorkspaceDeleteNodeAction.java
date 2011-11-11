/**
 * author: Marcel Genzmehr
 * 10.11.2011
 */
package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.EnabledAction;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

/**
 * 
 */
@EnabledAction(checkOnNodeChange = true, checkOnPopup = true)
public class WorkspaceDeleteNodeAction extends AWorkspaceAction {

	private static final long serialVersionUID = -8965412338727545850L;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public WorkspaceDeleteNodeAction() {
		super("workspace.action.node.delete");
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public void setEnabledFor(AWorkspaceTreeNode node) {
		if(node.isSystem()) {
			setEnabled(false);
		}
		else{
			setEnabled();
		}
	}
	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}
}
