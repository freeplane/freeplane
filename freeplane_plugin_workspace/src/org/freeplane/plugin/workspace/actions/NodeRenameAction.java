package org.freeplane.plugin.workspace.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.components.dialog.NodeRenameDialogPanel;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.IMutableLinkNode;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;

@CheckEnableOnPopup
public class NodeRenameAction extends AWorkspaceAction {

	public static final String KEY = "workspace.action.node.rename";
	private static final long serialVersionUID = 1L;

	public NodeRenameAction() {
		super(KEY);
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public void setEnabledFor(AWorkspaceTreeNode node, TreePath[] selectedPaths) {
		if(node.isSystem() || selectedPaths.length > 1) {
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
		String newName = panel.getText();
		
		if(okButton != JOptionPane.OK_OPTION || oldName.equals(newName)) {
			return;
		}
		
		if (newName != null) {
			if (targetNode instanceof IMutableLinkNode) {
				if (((IMutableLinkNode) targetNode).changeName(newName, panel.applyChangesForLink())) {
					targetNode.refresh();
				} 
				else {
					JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("error_rename_file"), 
							TextUtils.getText("error_rename_file_title"), JOptionPane.ERROR_MESSAGE);
					targetNode.setName(oldName);
				}
			}
			else {
				try {
					targetNode.getModel().changeNodeName(targetNode, newName);
					targetNode.refresh();
				}
				catch(Exception ex) {
					JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("error_rename_file") + " ("+ex.getMessage()+")", 
							TextUtils.getText("error_rename_file_title"), JOptionPane.ERROR_MESSAGE);
				}
			}
			
		}

	}

}
