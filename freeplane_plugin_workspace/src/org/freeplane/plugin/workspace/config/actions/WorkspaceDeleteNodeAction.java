/**
 * author: Marcel Genzmehr
 * 10.11.2011
 */
package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.model.action.AWorkspaceAction;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

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
		int option = JOptionPane.showConfirmDialog(UITools.getFrame(), TextUtils.getRawText("workspace.action.node.delete.confirm.text", "Do you really want to remove ") + getNodeFromActionEvent(e).getName());
		if(option == JOptionPane.YES_OPTION) {
			WorkspaceUtils.getModel().removeNodeFromParent(getNodeFromActionEvent(e));
			WorkspaceUtils.saveCurrentConfiguration();
			WorkspaceController.getController().refreshWorkspace();
		}
	}
}
