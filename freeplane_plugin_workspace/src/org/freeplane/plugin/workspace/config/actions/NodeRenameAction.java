package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.dialog.NodeRenameDialogPanel;
import org.freeplane.plugin.workspace.io.node.DefaultFileNode;
import org.freeplane.plugin.workspace.model.action.AWorkspaceAction;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.node.IMutableLinkNode;

@EnabledAction(checkOnPopup = true)
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
		NodeRenameDialogPanel panel;
//		if (targetNode instanceof IMutableLinkNode) {
//			panel = new NodeRenameDialogPanel(oldName, true);
//		} 
//		else {
			panel = new NodeRenameDialogPanel(oldName);
			if(targetNode instanceof DefaultFileNode) {
				panel.setCheckboxSelected(true);
			}
//		}
		int okButton = JOptionPane.showConfirmDialog(UITools.getFrame(), panel, TextUtils.getText("confirm_rename_file_action"),  JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		String newName = panel.getName();
		
		if(okButton != JOptionPane.OK_OPTION || oldName.equals(newName)) {
			return;
		}
		
		if (newName != null) {
			if (targetNode instanceof IMutableLinkNode) {
				if (((IMutableLinkNode) targetNode).changeName(newName, panel.applyChangesForLink())) {
					WorkspaceUtils.saveCurrentConfiguration();
					targetNode.refresh();
				} 
				else {
					JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("error_rename_file"), 
							TextUtils.getText("error_rename_file_title"), JOptionPane.ERROR_MESSAGE);
					targetNode.setName(oldName);
				}
			}
			else {
				targetNode.setName(newName);
				WorkspaceUtils.saveCurrentConfiguration();
				targetNode.refresh();
			}
			
		}

	}

}
