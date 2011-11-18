package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.io.node.DefaultFileNode;
import org.freeplane.plugin.workspace.model.action.AWorkspaceAction;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

@EnabledAction(checkOnNodeChange = true, checkOnPopup = true)
public class NodeRenameAction extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NodeRenameAction() {
		super("workspace.action.node.rename");
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

	public void actionPerformed(final ActionEvent e) {
		AWorkspaceTreeNode targetNode = this.getNodeFromActionEvent(e);
		
		String oldName = targetNode.getName();		
		String newName = JOptionPane.showInputDialog(Controller.getCurrentController().getViewController().getContentPane(),
				TextUtils.getText("confirm_rename_file_action"), oldName);

		if (newName != null) {		
			if (targetNode instanceof DefaultFileNode) {
				if (((DefaultFileNode) targetNode).rename(newName)) {									
					WorkspaceUtils.getModel().valueForPathChanged(targetNode.getTreePath(), newName);
					WorkspaceUtils.saveCurrentConfiguration();
				}
				else {
					JOptionPane.showMessageDialog(Controller.getCurrentController().getViewController().getContentPane(),
							TextUtils.getText("error_rename_file"), TextUtils.getText("error_rename_file_title"), 
							JOptionPane.ERROR_MESSAGE);
				}

			}
			else {
				targetNode.setName(newName);
				WorkspaceUtils.saveCurrentConfiguration();
			}
		}

	}

}
